package com.github.aidensuen.kafkatool.common.notify.model;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.notification.Notification;

public class ErrorNotification extends KafkaToolNotification {
    private static final String ICON_PATH = "/icons/error.png";

    private ErrorNotification(String content) {
        super(content, NotificationType.ERROR);
        this.setIcon(IconLoader.getIcon("/icons/error.png"));
    }

    public static Notification create(String content) {
        return new ErrorNotification(content);
    }
}