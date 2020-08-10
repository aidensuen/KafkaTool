package com.github.aidensuen.kafkatool.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.aidensuen.kafkatool.common.exception.JsonToAvroException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Component
public class AvroJsonGenerator {

    private final JsonNodeFactory jsonNodeFactory;

    AvroJsonGenerator() {
        this.jsonNodeFactory = JsonNodeFactory.instance;
    }

    public JsonNode generate(Schema schema, java.util.function.Function<Schema, Integer> unionSelector) {
        Object jsonNode;
        if (schema.getType() == Type.RECORD) {
            jsonNode = this.resolveRecord(schema, unionSelector);
        } else if (schema.getType() == Type.ENUM) {
            jsonNode = this.jsonNodeFactory.textNode(schema.getEnumSymbols().get((new Random(Instant.now().toEpochMilli())).nextInt(schema.getEnumSymbols().size() - 1)));
        } else if (schema.getType() == Type.ARRAY) {
            jsonNode = this.jsonNodeFactory.arrayNode();
            ((ArrayNode) jsonNode).add(this.generate(schema.getElementType(), unionSelector));
        } else if (schema.getType() == Type.MAP) {
            jsonNode = this.jsonNodeFactory.objectNode();
        } else if (schema.getType() == Type.UNION) {
            jsonNode = this.generate(schema.getTypes().get(unionSelector.apply(schema)), unionSelector);
        } else if (schema.getType() == Type.STRING) {
            jsonNode = this.jsonNodeFactory.textNode("");
        } else if (schema.getType() != Type.BYTES && schema.getType() != Type.FIXED) {
            if (schema.getType() == Type.INT) {
                jsonNode = this.jsonNodeFactory.numberNode(new Random().nextInt());
            } else if (schema.getType() == Type.LONG) {
                jsonNode = this.jsonNodeFactory.numberNode(Instant.now().toEpochMilli());
            } else if (schema.getType() == Type.FLOAT) {
                jsonNode = this.jsonNodeFactory.numberNode(new Random().nextFloat());
            } else if (schema.getType() == Type.DOUBLE) {
                jsonNode = this.jsonNodeFactory.numberNode(new Random().nextDouble());
            } else if (schema.getType() == Type.BOOLEAN) {
                jsonNode = this.jsonNodeFactory.booleanNode(false);
            } else {
                if (schema.getType() != Type.NULL) {
                    throw new JsonToAvroException("Unable to determine schema type");
                }

                jsonNode = this.jsonNodeFactory.nullNode();
            }
        } else {
            jsonNode = this.jsonNodeFactory.binaryNode("".getBytes(StandardCharsets.UTF_8));
        }

        return (JsonNode) jsonNode;
    }

    private JsonNode resolveRecord(Schema schema, Function<Schema, Integer> unionSelector) {
        ObjectNode objectNode = new ObjectNode(JsonNodeFactory.instance);
        List<Field> fields = schema.getFields();
        fields.forEach((field) -> {
            objectNode.replace(field.name(), this.generate(field.schema(), unionSelector));
        });
        return objectNode;
    }
}
