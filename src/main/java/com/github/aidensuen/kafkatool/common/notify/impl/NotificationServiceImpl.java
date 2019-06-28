package com.github.aidensuen.kafkatool.common.notify.impl;

import com.github.aidensuen.kafkatool.common.notify.NotificationService;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications.Bus;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final String GROUP_NOTIFICATION_ID = "KafkaToolNotification";

    public NotificationServiceImpl() {
    }

    public void info(String message) {
        Bus.notify(new Notification("KafkaToolNotification", "Info", message, NotificationType.INFORMATION));
    }

    public void success(String message) {
        Bus.notify(new Notification("KafkaToolNotification", "Success", message, NotificationType.INFORMATION));
    }

    public void error(String message) {
        Bus.notify(new Notification("KafkaToolNotification", "Error", message, NotificationType.ERROR));
    }
}
