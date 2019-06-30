package com.github.aidensuen.kafkatool.compents.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.aidensuen.kafkatool.common.notify.NotificationService;
import com.github.aidensuen.kafkatool.common.service.KafkaManagerService;
import com.github.aidensuen.kafkatool.compents.KafkaToolComponent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class KafkaConsumerContainerComponent implements KafkaToolComponent, DumbAware {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaManagerService kafkaManagerService;

    @Autowired
    private NotificationService notificationService;

    private JPanel mainPanel;
    private JButton createConsumerButton;
    private JTabbedPane consumerTabbedPane;
    private JComboBox<String> deserializerComboBox;
    private JTextField topicTextField;

    public KafkaConsumerContainerComponent() {
        initUI();
    }

    private void initUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        deserializerComboBox = new ComboBox<>();
        jPanel.add(deserializerComboBox, BorderLayout.WEST);

        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(new BorderLayout());

        topicTextField = new JTextField();
        topicTextField.setText("Topic");
        topicTextField.setToolTipText("Topic");
        jPanel1.add(topicTextField, BorderLayout.CENTER);

        createConsumerButton = new JButton("Create");
        jPanel1.add(createConsumerButton, BorderLayout.EAST);

        jPanel.add(jPanel1, BorderLayout.CENTER);

        consumerTabbedPane = new JBTabbedPane();
        mainPanel.add(jPanel, BorderLayout.NORTH);
        mainPanel.add(consumerTabbedPane, BorderLayout.CENTER);
    }

    @Override
    public Content getContent(@NotNull Project project) {
        this.deserializerComboBox.addItem("StringDeserializer");
        this.deserializerComboBox.addItem("KafkaAvroDeserializer");
        this.createConsumerButton.addActionListener((e) -> {
            String topic = this.topicTextField.getText();
            if (!topic.isEmpty()) {
                this.consumerTabbedPane.add(topic, (new KafkaConsumerComponent(kafkaManagerService, objectMapper, deserializerComboBox.getSelectedItem().toString(), topic)).getContent(project).getComponent());
            } else {
                this.notificationService.error("Invalid Topic");
            }

        });

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        return contentFactory.createContent(this.mainPanel, "Consumer", false);
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
