package com.github.aidensuen.kafkatool.common.exception;

public class JsonToAvroException extends RuntimeException {
    public JsonToAvroException(String message) {
        super(message);
    }

    public JsonToAvroException(String message, Throwable cause) {
        super(message, cause);
    }
}

