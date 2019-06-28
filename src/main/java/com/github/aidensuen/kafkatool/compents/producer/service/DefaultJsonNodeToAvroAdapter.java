package com.github.aidensuen.kafkatool.compents.producer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.aidensuen.kafkatool.common.exception.KafkaToolException;
import com.google.common.collect.Lists;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Service;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData.Fixed;
import org.apache.avro.generic.GenericData.Record;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Service
public class DefaultJsonNodeToAvroAdapter implements BiFunction<JsonNode, Schema, Object> {
    public DefaultJsonNodeToAvroAdapter() {
    }

    public Object apply(JsonNode jsonNode, Schema schema) {
        Object parsedObject;
        if (schema.getType() == Type.RECORD) {
            parsedObject = this.resolveRecord(schema, jsonNode);
        } else if (schema.getType() == Type.ENUM) {
            parsedObject = resolveEnum(schema, jsonNode);
        } else if (schema.getType() == Type.ARRAY && jsonNode.isArray()) {
            parsedObject = this.resolveArray(schema, jsonNode);
        } else if (schema.getType() == Type.MAP) {
            parsedObject = this.resolveMap(schema, jsonNode);
        } else if (schema.getType() == Type.UNION) {
            parsedObject = this.resolveUnion(schema, jsonNode);
        } else if (schema.getType() == Type.FIXED) {
            parsedObject = new Fixed(schema, jsonNode.textValue().getBytes(Charset.forName("UTF-8")));
        } else if (schema.getType() == Type.STRING && jsonNode.textValue() != null && jsonNode.isTextual()) {
            parsedObject = jsonNode.textValue();
        } else if (schema.getType() == Type.BYTES && jsonNode.isTextual()) {
            parsedObject = ByteBuffer.wrap(jsonNode.textValue().getBytes(Charset.forName("UTF-8")));
        } else if (schema.getType() == Type.INT && jsonNode.isNumber()) {
            parsedObject = jsonNode.intValue();
        } else if (schema.getType() == Type.LONG && jsonNode.isNumber()) {
            parsedObject = jsonNode.longValue();
        } else if (schema.getType() == Type.FLOAT && jsonNode.isNumber()) {
            parsedObject = jsonNode.floatValue();
        } else if (schema.getType() == Type.DOUBLE && jsonNode.isNumber()) {
            parsedObject = jsonNode.doubleValue();
        } else if (schema.getType() == Type.BOOLEAN && isBooleanNode(jsonNode)) {
            parsedObject = jsonNode.asBoolean();
        } else {
            if (schema.getType() != Type.NULL || !jsonNode.isNull()) {
                throw new KafkaToolException("Unable to determine schema type");
            }

            parsedObject = null;
        }

        return parsedObject;
    }

    private static boolean isBooleanNode(JsonNode jsonNode) {
        return jsonNode.isBoolean() || jsonNode.isTextual() && (jsonNode.textValue().equalsIgnoreCase("true") || jsonNode.textValue().equalsIgnoreCase("false"));
    }

    private static Object resolveEnum(Schema schema, JsonNode jsonNode) {
        return (new GenericData()).createEnum(jsonNode.textValue(), schema);
    }

    private Object resolveUnion(Schema schema, JsonNode jsonNode) {
        List<Schema> possibleSchemas = schema.getTypes();
        Object unionObject = null;
        Schema resolvedSchema = null;
        boolean schemaFound = false;

        for (int i = 0; i < possibleSchemas.size() && !schemaFound; ++i) {
            try {
                unionObject = this.apply(jsonNode, (Schema) possibleSchemas.get(i));
                resolvedSchema = (Schema) possibleSchemas.get(i);
                schemaFound = true;
            } catch (KafkaToolException var10) {
            }
        }

        if (schemaFound && (unionObject != null || resolvedSchema == null || resolvedSchema.getType() == Type.NULL)) {
            return unionObject;
        } else {
            throw new KafkaToolException("Unable to resolve union");
        }
    }

    private Object resolveMap(Schema schema, JsonNode jsonNode) {
        Schema mapValueSchema = schema.getValueType();
        Map<String, Object> map = new HashMap();
        jsonNode.fields().forEachRemaining((field) -> {
            map.put(field.getKey(), this.apply((JsonNode) field.getValue(), mapValueSchema));
        });
        return map;
    }

    private Object resolveArray(Schema schema, JsonNode jsonNode) {
        List<JsonNode> elements = Lists.newArrayList(jsonNode.elements());
        Schema elementType = schema.getElementType();
        List<Object> list = new ArrayList();
        elements.forEach((element) -> {
            list.add(this.apply(element, elementType));
        });
        return list;
    }

    private Object resolveRecord(Schema schema, JsonNode jsonNode) {
        GenericRecord genericRecord = new Record(schema);
        List<Field> fields = schema.getFields();
        fields.forEach((field) -> {
            if (jsonNode.has(field.name())) {
                genericRecord.put(field.name(), this.apply(jsonNode.get(field.name()), field.schema()));
            } else {
                throw new KafkaToolException("");
            }
        });
        return genericRecord;
    }
}
