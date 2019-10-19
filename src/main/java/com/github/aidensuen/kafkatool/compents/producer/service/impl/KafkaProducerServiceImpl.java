package com.github.aidensuen.kafkatool.compents.producer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.aidensuen.kafkatool.common.AvroJsonGenerator;
import com.github.aidensuen.kafkatool.common.Function;
import com.github.aidensuen.kafkatool.common.KafkaToolPersistentStateComponent;
import com.github.aidensuen.kafkatool.common.KafkaToolSerializerRepository;
import com.github.aidensuen.kafkatool.common.collection.FixedStack;
import com.github.aidensuen.kafkatool.common.exception.KafkaToolException;
import com.github.aidensuen.kafkatool.common.notify.NotificationService;
import com.github.aidensuen.kafkatool.common.notify.model.ErrorNotification;
import com.github.aidensuen.kafkatool.common.service.KafkaManagerService;
import com.github.aidensuen.kafkatool.compents.producer.KafkaProducerComponent;
import com.github.aidensuen.kafkatool.compents.producer.service.AvroClassScanner;
import com.github.aidensuen.kafkatool.compents.producer.service.KafkaProducerProvider;
import com.github.aidensuen.kafkatool.compents.producer.service.KafkaProducerService;
import com.github.aidensuen.kafkatool.model.ProducerHistoryEntry;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerComponent.class);
    @Autowired
    public KafkaProducerProvider kafkaProducerProvider;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent;
    private Map<String, Class> avroClassMap = new ConcurrentHashMap<>();
    @Autowired
    private ExecutorService executorService;
    @Autowired
    private KafkaManagerService kafkaManagerService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AvroClassScanner avroClassScanner;
    @Autowired
    private KafkaToolSerializerRepository kafkaToolSerializerRepository;

    @Autowired
    private BiFunction biFunction;

    @Autowired
    private AvroJsonGenerator avroJsonGenerator;

    @Override
    public void getTopicList(Function<List<String>> function) {
        kafkaManagerService.getTopicList(list -> {
            function.callBack(list);
        });
    }

    @Override
    public Set<String> getAllSerializers() {
        return this.kafkaToolSerializerRepository.getAllSerializers();
    }

    @Override
    public void refreshAvroClassList(Project project, Function<List<String>> function) {
        executorService.submit(() -> {
            List<String> result = Optional.ofNullable(project).map((nonNullProject) -> {
                try {
                    Set<Class> classesInPackage = avroClassScanner.loadAvroClassesFromProject(project, kafkaToolPersistentStateComponent.getAvroPackagePrefix());
                    if (classesInPackage.isEmpty()) {
                        notificationService.info("No classes found. Make sure you have Avro classes that extends " + SpecificRecordBase.class.getName() + " on your classpath and that you've properly configured the 'Avro Package Prefix' property under the 'Settings' tab.");
                    }
                    this.avroClassMap.clear();
                    return classesInPackage.stream().peek((aClass) -> {
                        this.avroClassMap.put(aClass.getName(), aClass);
                    }).map(Class::getName).sorted().collect(Collectors.toList());
                } catch (Exception e) {
                    Notifications.Bus.notify(ErrorNotification.create("Failed to refresh Avro classes." + e.getMessage()));
                }
                return new ArrayList<String>();
            }).orElseGet(() -> {
                notificationService.error("Failed to refresh Avro classes.");
                return Collections.emptyList();
            });
            function.callBack(result);

        });
    }

    @Override
    public String getAvroSchema(String className) {
        Class aClass = Objects.requireNonNull(this.avroClassMap.get(className));
        String schema = "";

        try {
            Field schemaField = aClass.getField("SCHEMA$");
            schema = (new Schema.Parser()).parse(schemaField.get(null).toString()).toString(true);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOGGER.error(e.getMessage());
            Notifications.Bus.notify(ErrorNotification.create("Failed to populate Avro schema."));
        }

        return schema;
    }

    @Override
    public void produceMessage(String topic, Integer partition, String serializerKey, String rawSchema, String key, String payload) {
        executorService.submit(() -> {
            Objects.requireNonNull(topic, "Topic field is required");
            Objects.requireNonNull(serializerKey, "Serializer field is required");
            Objects.requireNonNull(payload, "Payload field is required");
            Object message;
            if ("KafkaAvroSerializer".equals(serializerKey)) {
                try {
                    JsonNode jsonNode = this.objectMapper.readTree(payload);
                    Schema schema = (new Schema.Parser()).parse(rawSchema);
                    message = this.biFunction.apply(jsonNode, schema);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                    this.notificationService.error("Failed to serialize payload. Check that the schema and payload are correct.");
                    throw new KafkaToolException("Failed to serialize payload. Check that the schema and payload are correct.", e);
                }
            } else {
                message = payload;
            }

            KafkaTemplate kafkaTemplate = kafkaProducerProvider.get(serializerKey);

            try {
                ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, partition, key, message);
                future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        notificationService.error("Failed produced message.");
                    }

                    @Override
                    public void onSuccess(SendResult<String, Object> result) {
                        notificationService.success("Successfully produced message.");
                    }
                });
                addProducerHistoryEntry(topic, serializerKey, rawSchema, key, payload);
            } catch (Exception e) {
                this.notificationService.error(e.getMessage());
            }
        });
    }

    @Override
    public FixedStack<ProducerHistoryEntry> getProducerHistory() {
        return this.kafkaToolPersistentStateComponent.getProducerHistoryDataStack();
    }

    @Override
    public void clearProducerHistory() {
        this.kafkaToolPersistentStateComponent.getProducerHistoryDataStack().clear();
    }

    @Override
    public void generateRandomPayloadFromSchema(String rawSchema, Function<String> function) {
        executorService.submit(() -> {
            String result = "";
            try {
                result = Optional.ofNullable(rawSchema).map((nonNullRawSchema) -> {
                    Schema schema = (new Schema.Parser()).parse(nonNullRawSchema);
                    JsonNode generate = this.avroJsonGenerator.generate(schema, (schema1) -> {
                        return 1 + (new Random(Instant.now().toEpochMilli())).nextInt(schema1.getTypes().size() - 1);
                    });

                    String jsonResult;
                    try {
                        jsonResult = this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(generate);
                    } catch (JsonProcessingException var6) {
                        this.notificationService.error("Failed to make JSON pretty.");
                        jsonResult = "";
                    }

                    return jsonResult;
                }).orElseGet(() -> {
                    this.notificationService.error("Schema cannot be null.");
                    return "";
                });
            } catch (Exception e) {
                this.notificationService.error("Generate Schema failed, please check the schema!");
            }
            function.callBack(result);
        });
    }

    private void addProducerHistoryEntry(String topic, String serializer, String schema, String key, String payload) {
        ProducerHistoryEntry producerHistoryEntry = ProducerHistoryEntry.newBuilder().setId(UUID.randomUUID().toString()).setTopic(topic).setSerializerClass(serializer).setSchema(schema).setKey(key).setPayload(payload).setTimestamp(String.valueOf(Instant.now().toEpochMilli())).build();
        this.kafkaToolPersistentStateComponent.addProducerHistoryEntry(producerHistoryEntry);
    }


    public KafkaToolSerializerRepository getKafkaToolSerializerRepository() {
        return kafkaToolSerializerRepository;
    }

    public void setKafkaToolSerializerRepository(KafkaToolSerializerRepository kafkaToolSerializerRepository) {
        this.kafkaToolSerializerRepository = kafkaToolSerializerRepository;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public AvroClassScanner getAvroClassScanner() {
        return avroClassScanner;
    }

    public void setAvroClassScanner(AvroClassScanner avroClassScanner) {
        this.avroClassScanner = avroClassScanner;
    }

    public KafkaToolPersistentStateComponent getKafkaToolPersistentStateComponent() {
        return kafkaToolPersistentStateComponent;
    }

    public void setKafkaToolPersistentStateComponent(KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent) {
        this.kafkaToolPersistentStateComponent = kafkaToolPersistentStateComponent;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public KafkaProducerProvider getKafkaProducerProvider() {
        return kafkaProducerProvider;
    }

    public void setKafkaProducerProvider(KafkaProducerProvider kafkaProducerProvider) {
        this.kafkaProducerProvider = kafkaProducerProvider;
    }

    public BiFunction getBiFunction() {
        return biFunction;
    }

    public void setBiFunction(BiFunction biFunction) {
        this.biFunction = biFunction;
    }

    public AvroJsonGenerator getAvroJsonGenerator() {
        return avroJsonGenerator;
    }

    public void setAvroJsonGenerator(AvroJsonGenerator avroJsonGenerator) {
        this.avroJsonGenerator = avroJsonGenerator;
    }

    public KafkaManagerService getKafkaManagerService() {
        return kafkaManagerService;
    }

    public void setKafkaManagerService(KafkaManagerService kafkaManagerService) {
        this.kafkaManagerService = kafkaManagerService;
    }
}
