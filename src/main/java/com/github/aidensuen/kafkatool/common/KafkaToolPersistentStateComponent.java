package com.github.aidensuen.kafkatool.common;

import com.github.aidensuen.kafkatool.common.collection.FixedStack;
import com.github.aidensuen.kafkatool.model.ProducerHistoryEntry;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@State(
        name = "KafkaToolPersistentStateComponent",
        storages = {@Storage("kafka-tool-state.xml")}
)
@Component
public class KafkaToolPersistentStateComponent implements PersistentStateComponent<KafkaToolPersistentStateComponent> {
    private String bootstrapServers = "localhost:9092";
    private FixedStack<ProducerHistoryEntry> producerHistoryDataStack = FixedStack.newFixedStack();
    private String schemaRegistryUrl = "http://localhost:8081";
    private String avroPackagePrefix = "com.example";

    @Autowired
    private KafkaProperties kafkaProperties;

    @Nullable
    public KafkaToolPersistentStateComponent getState() {
        return this;
    }

    public void loadState(KafkaToolPersistentStateComponent state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getBootstrapServers() {
        return this.bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public FixedStack<ProducerHistoryEntry> getProducerHistoryDataStack() {
        return this.producerHistoryDataStack;
    }

    public void setProducerHistoryDataStack(FixedStack<ProducerHistoryEntry> producerHistoryDataStack) {
        this.producerHistoryDataStack = producerHistoryDataStack;
    }

    public String getSchemaRegistryUrl() {
        return this.schemaRegistryUrl;
    }

    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public void addProducerHistoryEntry(ProducerHistoryEntry producerHistoryEntry) {
        this.producerHistoryDataStack.push(producerHistoryEntry);
    }

    public String getAvroPackagePrefix() {
        return this.avroPackagePrefix;
    }

    public void setAvroPackagePrefix(String avroPackagePrefix) {
        this.avroPackagePrefix = avroPackagePrefix;
    }

    public Map<String, Object> getProducerProperties() {
        Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
        producerProperties.putIfAbsent("schema.registry.url", this.getSchemaRegistryUrl());
        producerProperties.putIfAbsent("bootstrap.servers", this.getBootstrapServers());
        return producerProperties;
    }


    public Map<String, Object> getConsumerProperties() {
        Map<String, Object> consumerProperties = kafkaProperties.buildProducerProperties();
        consumerProperties.putIfAbsent("schema.registry.url", this.getSchemaRegistryUrl());
        consumerProperties.putIfAbsent("bootstrap.servers", this.getBootstrapServers());
        return consumerProperties;
    }

    public KafkaProperties getKafkaProperties() {
        return kafkaProperties;
    }

    public void setKafkaProperties(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    public void refresh() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaToolPersistentStateComponent that = (KafkaToolPersistentStateComponent) o;
        return Objects.equals(bootstrapServers, that.bootstrapServers) &&
                Objects.equals(producerHistoryDataStack, that.producerHistoryDataStack) &&
                Objects.equals(schemaRegistryUrl, that.schemaRegistryUrl) &&
                Objects.equals(avroPackagePrefix, that.avroPackagePrefix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bootstrapServers, producerHistoryDataStack, schemaRegistryUrl, avroPackagePrefix);
    }
}

