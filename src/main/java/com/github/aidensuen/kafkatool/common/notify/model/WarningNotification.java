package com.github.aidensuen.kafkatool.common.notify.model;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;

public class WarningNotification extends KafkaToolNotification {

    private WarningNotification(String content) {
        super(content, NotificationType.WARNING);
    }

    private WarningNotification(String content, String title) {
        super(content, title, NotificationType.WARNING);
    }

    public static Notification create(String content) {
        return new WarningNotification(content);
    }

    public static Notification create(String content, String title) {
        return new WarningNotification(content, title);
    }
}
