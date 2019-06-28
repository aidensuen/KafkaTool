package com.github.aidensuen.kafkatool.common;

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class KafkaToolSerializerRepository {

    private static final Map<String, Class<? extends Serializer>> SERIALIZER_MAP = ImmutableMap.of("StringSerializer", StringSerializer.class);

    public KafkaToolSerializerRepository() {
    }

    public Class<? extends Serializer> getSerializerByKey(String key) {
        return SERIALIZER_MAP.get(key);
    }

    public Set<String> getAllSerializers() {
        return SERIALIZER_MAP.keySet();
    }
}
