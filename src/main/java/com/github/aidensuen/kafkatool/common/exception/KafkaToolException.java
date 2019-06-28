package com.github.aidensuen.kafkatool.common.exception;

public class KafkaToolException extends RuntimeException {
    public KafkaToolException(String message) {
        super(message);
    }

    public KafkaToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
