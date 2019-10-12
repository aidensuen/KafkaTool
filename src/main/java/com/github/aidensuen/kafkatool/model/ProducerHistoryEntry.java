package com.github.aidensuen.kafkatool.model;


import java.time.Instant;
import java.util.Objects;

public class ProducerHistoryEntry {
    private String id;
    private String topic;
    private String serializer;
    private String schema;
    private String key;
    private String payload;
    private String timestamp;

    private ProducerHistoryEntry() {
        this.id = "id";
        this.topic = "topic";
        this.serializer = "";
        this.schema = "schema";
        this.key = "key";
        this.payload = "payload";
        this.timestamp = Instant.now().toString();
    }

    public static ProducerHistoryEntry.ProducerHistoryEntryBuilder newBuilder() {
        return new ProducerHistoryEntry.ProducerHistoryEntryBuilder();
    }

    public String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSerializer() {
        return this.serializer;
    }

    private void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getKey() {
        return this.key;
    }

    private void setKey(String key) {
        this.key = key;
    }

    public String getPayload() {
        return this.payload;
    }

    private void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    private void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("ProducerHistoryEntry{");
        stringBuilder.append("id='").append(this.id).append('\'');
        stringBuilder.append(", topic='").append(this.topic).append('\'');
        stringBuilder.append(", serializer='").append(this.serializer).append('\'');
        stringBuilder.append(", schema='").append(this.schema).append('\'');
        stringBuilder.append(", key='").append(this.key).append('\'');
        stringBuilder.append(", payload='").append(this.payload).append('\'');
        stringBuilder.append(", timestamp='").append(this.timestamp).append('\'');
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ProducerHistoryEntry that = (ProducerHistoryEntry) o;
            return Objects.equals(this.id, that.id) && Objects.equals(this.topic, that.topic) && Objects.equals(this.serializer, that.serializer) && Objects.equals(this.schema, that.schema) && Objects.equals(this.key, that.key) && Objects.equals(this.payload, that.payload) && Objects.equals(this.timestamp, that.timestamp);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.topic, this.serializer, this.schema, this.key, this.payload, this.timestamp});
    }

    public static final class ProducerHistoryEntryBuilder {
        private ProducerHistoryEntry producerHistoryEntry;

        private ProducerHistoryEntryBuilder() {
            this.producerHistoryEntry = new ProducerHistoryEntry();
        }

        public ProducerHistoryEntry.ProducerHistoryEntryBuilder setId(String id) {
            this.producerHistoryEntry.setId(id);
            return this;
        }

        public ProducerHistoryEntry.ProducerHistoryEntryBuilder setTopic(String topic) {
            this.producerHistoryEntry.setTopic(topic);
            return this;
        }

        public ProducerHistoryEntry.ProducerHistoryEntryBuilder setSerializerClass(String serializerClass) {
            this.producerHistoryEntry.setSerializer(serializerClass);
            return this;
        }

        public ProducerHistoryEntry.ProducerHistoryEntryBuilder setSchema(String schema) {
            this.producerHistoryEntry.setSchema(schema);
            return this;
        }

        public ProducerHistoryEntry.ProducerHistoryEntryBuilder setKey(String key) {
            this.producerHistoryEntry.setKey(key);
            return this;
        }

        public ProducerHistoryEntry.ProducerHistoryEntryBuilder setPayload(String payload) {
            this.producerHistoryEntry.setPayload(payload);
            return this;
        }

        public ProducerHistoryEntry.ProducerHistoryEntryBuilder setTimestamp(String timestamp) {
            this.producerHistoryEntry.setTimestamp(timestamp);
            return this;
        }

        public ProducerHistoryEntry build() {
            return this.producerHistoryEntry;
        }
    }
}
