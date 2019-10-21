package com.github.aidensuen.kafkatool.common.notify;

public interface NotificationService {

    void info(String message);

    void success(String message);

    void error(String message);

    void warning(String message);
}
