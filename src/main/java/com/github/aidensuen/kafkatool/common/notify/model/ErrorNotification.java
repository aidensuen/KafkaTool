package com.github.aidensuen.kafkatool.common.notify.model;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.IconLoader;

public class ErrorNotification extends KafkaToolNotification {

    private static final String ICON_PATH = "/icons/error.png";

    private ErrorNotification(String content) {
        super(content, NotificationType.ERROR);
        this.setIcon(IconLoader.getIcon(ICON_PATH));
    }

    private ErrorNotification(String content, String title) {
        super(content, title, NotificationType.ERROR);
        this.setIcon(IconLoader.getIcon(ICON_PATH));
    }

    public static Notification create(String content) {
        return new ErrorNotification(content);
    }

    public static Notification create(String content, String title) {
        return new ErrorNotification(content, title);
    }
}