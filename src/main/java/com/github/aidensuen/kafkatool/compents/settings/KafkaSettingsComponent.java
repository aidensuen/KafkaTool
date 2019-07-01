package com.github.aidensuen.kafkatool.compents.settings;

import com.github.aidensuen.kafkatool.common.KafkaToolPersistentStateComponent;
import com.github.aidensuen.kafkatool.common.notify.NotificationService;
import com.github.aidensuen.kafkatool.compents.KafkaToolComponent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactory.SERVICE;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class KafkaSettingsComponent implements KafkaToolComponent, DumbAware {

    private static final String SETTINGS_LABEL = "Settings";
    private KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent;
    private NotificationService notificationService;

    private JPanel mainPanel;
    private JTextField bootstrapServersSettingField;
    private JTextField schemaRegistrySettingField;
    private JTextField avroPackagePrefixField;
    private JButton saveSettingsButton;
    private JButton restoreDefaultsButton;

    @Autowired
    public KafkaSettingsComponent(KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent, NotificationService notificationService) {
        this.kafkaToolPersistentStateComponent = kafkaToolPersistentStateComponent;
        this.notificationService = notificationService;
        this.initUI();
    }

    private void initUI() {
        this.mainPanel = new JPanel();

        JPanel controlPanel = new JPanel();

        controlPanel.setLayout(new GridLayout(7, 1, 0, 1));

        JLabel bootstrapLabel = new JLabel("Bootstrap Servers:");
        controlPanel.add(bootstrapLabel);

        this.bootstrapServersSettingField = new JTextField("Bootstrap Servers");
        controlPanel.add(this.bootstrapServersSettingField);

        JLabel schemaRegistryLabel = new JLabel("Schema Registry:");
        controlPanel.add(schemaRegistryLabel);

        this.schemaRegistrySettingField = new JTextField("Schema Registry:");
        controlPanel.add(this.schemaRegistrySettingField);

        JLabel avroPackageLabel = new JLabel("Avro Package Prefix:");
        controlPanel.add(avroPackageLabel);

        this.avroPackagePrefixField = new JTextField("Avro Package Prefix");
        controlPanel.add(this.avroPackagePrefixField);

        this.saveSettingsButton = new JButton("Save");

        this.restoreDefaultsButton = new JButton("Restore Defaults");

        JPanel buttonJpanel = new JPanel();
        buttonJpanel.setLayout(new GridLayout(1, 2, 1, 0));
        buttonJpanel.add(this.saveSettingsButton);
        buttonJpanel.add(this.restoreDefaultsButton);
        controlPanel.add(buttonJpanel);

        this.mainPanel.setLayout(new GridLayout(2, 1));
        this.mainPanel.add(controlPanel);
    }

    @Override
    public Content getContent(@NotNull Project project) {

        this.bootstrapServersSettingField.setText(this.kafkaToolPersistentStateComponent.getBootstrapServers());
        this.schemaRegistrySettingField.setText(this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl());
        this.avroPackagePrefixField.setText(this.kafkaToolPersistentStateComponent.getAvroPackagePrefix());
        this.avroPackagePrefixField.setText(this.kafkaToolPersistentStateComponent.getAvroPackagePrefix());
        this.saveSettingsButton.addActionListener((e) -> {
            this.kafkaToolPersistentStateComponent.setBootstrapServers(this.bootstrapServersSettingField.getText());
            this.kafkaToolPersistentStateComponent.setSchemaRegistryUrl(this.schemaRegistrySettingField.getText());
            this.kafkaToolPersistentStateComponent.setAvroPackagePrefix(this.avroPackagePrefixField.getText());
            this.notificationService.success("Saved");
        });
        this.restoreDefaultsButton.addActionListener((event) -> {
            this.schemaRegistrySettingField.setText("http://localhost:8081");
            this.bootstrapServersSettingField.setText("localhost:9091");
            this.avroPackagePrefixField.setText("com.example");
        });
        ContentFactory contentFactory = SERVICE.getInstance();
        return contentFactory.createContent(this.mainPanel, SETTINGS_LABEL, false);
    }
}
