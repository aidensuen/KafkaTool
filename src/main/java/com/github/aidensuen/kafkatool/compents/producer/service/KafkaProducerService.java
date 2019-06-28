package com.github.aidensuen.kafkatool.compents.producer.service;

import com.github.aidensuen.kafkatool.common.Function;
import com.github.aidensuen.kafkatool.common.collection.FixedStack;
import com.github.aidensuen.kafkatool.model.ProducerHistoryEntry;
import com.intellij.openapi.project.Project;

import java.util.List;
import java.util.Set;

public interface KafkaProducerService {

    void getTopicList(Function<List<String>> function);

    Set<String> getAllSerializers();

    void refreshAvroClassList(Project project, Function<List<String>> function);

    String getAvroSchema(String className);

    void produceMessage(String topic, Integer partition, String serializerKey, String rawSchema, String key, String payload);

    FixedStack<ProducerHistoryEntry> getProducerHistory();

    void clearProducerHistory();

    void generateRandomPayloadFromSchema(String rawSchema, Function<String> function);
}
