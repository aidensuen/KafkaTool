package com.github.aidensuen.kafkatool.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.aidensuen.kafkatool.common.Function;
import com.github.aidensuen.kafkatool.common.KafkaToolPersistentStateComponent;
import com.github.aidensuen.kafkatool.common.exception.KafkaToolException;
import com.github.aidensuen.kafkatool.common.notify.model.ErrorNotification;
import com.github.aidensuen.kafkatool.common.service.KafkaManagerService;
import com.github.aidensuen.kafkatool.model.SchemaVersion;
import com.github.aidensuen.kafkatool.model.Subject;
import com.intellij.notification.Notifications;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class KafkaManagerServiceImpl implements KafkaManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaManagerServiceImpl.class);

    private static final ConcurrentHashMap<String, Class> DESERIALIZER_CLASS_MAP = new ConcurrentHashMap();

    static {
        DESERIALIZER_CLASS_MAP.put("StringDeserializer", StringDeserializer.class);
        DESERIALIZER_CLASS_MAP.put("KafkaAvroDeserializer", KafkaAvroDeserializer.class);
    }

    @Autowired
    KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    public KafkaManagerServiceImpl() {
    }

    @Override
    public void getTopicList(Function<List<String>> function) {
        getDetailedTopicList(map -> {
            List<String> result = map.keySet().stream().sorted().collect(Collectors.toList());
            function.callBack(result);
        });

    }

    @Override
    public void getDetailedTopicList(Function<Map<String, List<PartitionInfo>>> function) {
        Properties props = new Properties();
        props.put("bootstrap.servers", this.kafkaToolPersistentStateComponent.getBootstrapServers());
        props.put("group.id", "kafka-tool-topic-registry");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", StringDeserializer.class);
        props.put("exclude.internal.topics", true);
        props.put("request.timeout.ms", 30000);
        props.put("session.timeout.ms", 15000);
        props.put("heartbeat.interval.ms", 5000);

        executorService.submit(() -> {
            Map<String, List<PartitionInfo>> map = null;
            try {
                try (Consumer<String, Object> consumer = consumerFactory(props).createConsumer()) {
                    map = consumer.listTopics();
                } catch (Throwable e) {
                    throw e;
                }
            } catch (Exception e) {
                Notifications.Bus.notify(ErrorNotification.create("Failed to refresh topic list.\n" + e.getMessage()));
                map = new HashMap<>();
            } finally {
                function.callBack(map);
            }
        });
    }

    @Override
    public void consume(String deserializer, String topic, int numberOfRecords, int readAttempts, Function<Optional<List<ConsumerRecord<String, Object>>>> function) {
        executorService.submit(() -> {
            List<ConsumerRecord<String, Object>> consumerRecords = new ArrayList();
            try {
                Consumer<String, Object> kafkaConsumer = this.consumerFactory(deserializer).createConsumer();
                kafkaConsumer.subscribe(Collections.singletonList(topic));
                for (int i = 0; i < readAttempts && consumerRecords.size() < numberOfRecords; i++) {
                    try {
                        ConsumerRecords<String, Object> records = kafkaConsumer.poll(Duration.ofSeconds(1000L));
                        consumerRecords.addAll(StreamSupport.stream(records.records(topic).spliterator(), false).collect(Collectors.toList()));
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            } catch (Exception e) {
                Notifications.Bus.notify(ErrorNotification.create(e.getMessage()));
            } finally {
                function.callBack(Optional.of(consumerRecords));
            }
        });
    }

    @Override
    public void listSubjects(Function<List<Subject>> function) {
        String listSubjectsUrl = String.format("%s/subjects", this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl());
        this.executorService.submit(() -> {
            try {
                List<String> subjects = this.restTemplate.getForObject(listSubjectsUrl, List.class, new Object[0]);
                List<Subject> subjectList = subjects.stream().map((subjectName) -> {
                    List<SchemaVersion> schemaVersionList = new ArrayList();
                    this.listSubjectVersions(subjectName, versons -> {
                        versons.forEach((version) -> {
                            String versionStr = String.valueOf(version);
                            this.getSchema(subjectName, versionStr, schema -> {
                                try {
                                    schemaVersionList.add(this.objectMapper.readValue(schema, SchemaVersion.class));
                                } catch (IOException e) {
                                    throw new KafkaToolException("Failed to parse schema version", e);
                                }
                            });
                        });
                    });
                    return Subject.newBuilder().setSchemaList(schemaVersionList).setSubjectName(subjectName).build();
                }).collect(Collectors.toList());
                function.callBack(subjectList);
            } catch (Exception e) {
                Notifications.Bus.notify(ErrorNotification.create(e.getMessage()));
                function.callBack(new ArrayList<>());
            }
        });
    }

    @Override
    public void listSubjectVersions(String subject, Function<List<Integer>> function) {
        String url = String.format("%s/subjects/%s/versions/", this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl(), subject);
        try {
            function.callBack(this.restTemplate.getForObject(url, List.class, new Object[0]));
        } catch (Exception e) {
            Notifications.Bus.notify(ErrorNotification.create(e.getMessage()));
            function.callBack(new ArrayList<>());
        }
    }

    @Override
    public void getSchema(String subject, String version, Function<String> function) {
        String url = String.format("%s/subjects/%s/versions/%s", this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl(), subject, version);
        try {
            function.callBack(this.restTemplate.getForObject(url, String.class, new Object[0]));
        } catch (Exception e) {
            Notifications.Bus.notify(ErrorNotification.create(e.getMessage()));
            function.callBack("");
        }
    }

    @Override
    public void deleteSchema(String subject, String version, Function<Boolean> function) {
        String url = String.format("%s/subjects/%s/versions/%s", this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl(), subject, version);
        this.executorService.submit(() -> {
            try {
                this.restTemplate.delete(url, new Object[0]);
                function.callBack(true);
            } catch (Exception e) {
                Notifications.Bus.notify(ErrorNotification.create(e.getMessage()));
                function.callBack(false);
            }

        });
    }

    private ConsumerFactory<String, Object> consumerFactory(String deserializer) {
        Map<String, Object> props = new HashMap();
        props.put("bootstrap.servers", this.kafkaToolPersistentStateComponent.getBootstrapServers());
        props.put("group.id", "kafkatool-consumer-group-id");
        props.put("key.deserializer", StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put("value.deserializer", DESERIALIZER_CLASS_MAP.get(deserializer));
        props.put("schema.registry.url", this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl());
        props.put("exclude.internal.topics", true);
        props.put("request.timeout.ms", 30000);
        props.put("session.timeout.ms", 15000);
        props.put("heartbeat.interval.ms", 5000);
        return new DefaultKafkaConsumerFactory(props);
    }

    private ConsumerFactory<String, Object> consumerFactory(Properties props) {
        return new DefaultKafkaConsumerFactory(props);
    }

    public KafkaToolPersistentStateComponent getKafkaToolPersistentStateComponent() {
        return kafkaToolPersistentStateComponent;
    }

    public void setKafkaToolPersistentStateComponent(KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent) {
        this.kafkaToolPersistentStateComponent = kafkaToolPersistentStateComponent;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
