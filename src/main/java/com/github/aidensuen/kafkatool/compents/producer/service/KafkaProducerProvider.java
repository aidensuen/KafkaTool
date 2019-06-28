package com.github.aidensuen.kafkatool.compents.producer.service;

import org.springframework.kafka.core.KafkaTemplate;

public interface KafkaProducerProvider<K, V> {

    KafkaTemplate<K, V> get(String serializerKey);
}

