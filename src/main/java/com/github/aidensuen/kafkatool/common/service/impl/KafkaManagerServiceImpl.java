package com.github.aidensuen.kafkatool.common.service.impl;

import com.github.aidensuen.kafkatool.common.Function;
import com.github.aidensuen.kafkatool.common.KafkaToolPersistentStateComponent;
import com.github.aidensuen.kafkatool.common.notify.model.ErrorNotification;
import com.github.aidensuen.kafkatool.common.service.KafkaManagerService;
import com.intellij.notification.Notifications;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Service;

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
        //DESERIALIZER_CLASS_MAP.put("KafkaAvroDeserializer", KafkaAvroDeserializer.class);
        DESERIALIZER_CLASS_MAP.put("StringDeserializer", StringDeserializer.class);
    }

    @Autowired
    KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent;

    @Autowired
    private ExecutorService executorService;

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
                Notifications.Bus.notify(ErrorNotification.create("Failed to refresh topic list. Is kafka running?"));
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
                LOGGER.error(e.getMessage());
            } finally {
                function.callBack(Optional.of(consumerRecords));
            }
        });
    }

    private ConsumerFactory<String, Object> consumerFactory(String deserializer) {
        Map<String, Object> props = new HashMap();
        props.put("bootstrap.servers", this.kafkaToolPersistentStateComponent.getBootstrapServers());
        props.put("group.id", "kafkasoft-consumer-group-id");
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
}