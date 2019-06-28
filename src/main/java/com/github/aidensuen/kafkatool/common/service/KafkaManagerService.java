package com.github.aidensuen.kafkatool.common.service;

import com.github.aidensuen.kafkatool.common.Function;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface KafkaManagerService {

    void getTopicList(Function<List<String>> topicList);

    void getDetailedTopicList(Function<Map<String, List<PartitionInfo>>> topicList);

    void consume(String deserializer, String topic, int numberOfRecords, int readAttempts, Function<Optional<List<ConsumerRecord<String, Object>>>> function);
}
