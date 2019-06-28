package com.github.aidensuen.kafkatool.compents.producer.service.impl;

import com.github.aidensuen.kafkatool.common.KafkaToolPersistentStateComponent;
import com.github.aidensuen.kafkatool.common.KafkaToolSerializerRepository;
import com.github.aidensuen.kafkatool.compents.producer.service.KafkaProducerProvider;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DefaultKafkaProducerProvider implements KafkaProducerProvider<String, Object> {

    @Autowired
    private KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent;

    @Autowired
    private KafkaToolSerializerRepository kafkaToolSerializerRepository;

    public DefaultKafkaProducerProvider() {
    }

    public KafkaTemplate<String, Object> get(String serializerKey) {
        Class<? extends Serializer> serializer = this.kafkaToolSerializerRepository.getSerializerByKey(serializerKey);
        Map<String, Object> props = new HashMap();
        props.put("bootstrap.servers", this.kafkaToolPersistentStateComponent.getBootstrapServers());
        props.put("schema.registry.url", this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put("value.serializer", serializer);
        props.put("acks", "1");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        props.put("max.block.ms", 3000);
        return new KafkaTemplate(new DefaultKafkaProducerFactory(props));
    }

    public KafkaToolPersistentStateComponent getKafkaToolPersistentStateComponent() {
        return kafkaToolPersistentStateComponent;
    }

    public void setKafkaToolPersistentStateComponent(KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent) {
        this.kafkaToolPersistentStateComponent = kafkaToolPersistentStateComponent;
    }

    public KafkaToolSerializerRepository getKafkaToolSerializerRepository() {
        return kafkaToolSerializerRepository;
    }

    public void setKafkaToolSerializerRepository(KafkaToolSerializerRepository kafkaToolSerializerRepository) {
        this.kafkaToolSerializerRepository = kafkaToolSerializerRepository;
    }
}