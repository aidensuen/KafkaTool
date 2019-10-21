package com.github.aidensuen.kafkatool.common.notify.impl;

import com.github.aidensuen.kafkatool.common.notify.NotificationService;
import com.github.aidensuen.kafkatool.common.notify.model.ErrorNotification;
import com.github.aidensuen.kafkatool.common.notify.model.SuccessNotification;
import com.github.aidensuen.kafkatool.common.notify.model.WarningNotification;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
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
        Bus.notify(SuccessNotification.create(message, "Success"));
    }

    public void error(String message) {
        Bus.notify(ErrorNotification.create(message, "Error"));
    }

    @Override
    public void warning(String message) {
        Bus.notify(WarningNotification.create(message, "Warning"));
    }
}
