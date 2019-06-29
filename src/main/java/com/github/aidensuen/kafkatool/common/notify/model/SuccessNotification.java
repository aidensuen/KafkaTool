package com.github.aidensuen.kafkatool.common.notify.model;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.util.IconLoader;

public class SuccessNotification extends KafkaToolNotification {
    private static final String ICON_PATH = "/icons/GreenOK.png";

    private SuccessNotification(String content) {
        super(content, NotificationType.INFORMATION);
        this.setIcon(IconLoader.getIcon(ICON_PATH));
    }

    private SuccessNotification(String content, String title) {
        super(content, title, NotificationType.INFORMATION);
        this.setIcon(IconLoader.getIcon(ICON_PATH));
    }

    public static Notification create(String content) {
        return new SuccessNotification(content);
    }

    public static Notification create(String content, String title) {
        return new SuccessNotification(content, title);
    }
}

