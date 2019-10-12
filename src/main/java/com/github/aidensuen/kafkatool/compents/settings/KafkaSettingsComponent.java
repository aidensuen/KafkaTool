package com.github.aidensuen.kafkatool.compents.settings;

import com.github.aidensuen.kafkatool.common.KafkaToolPersistentStateComponent;
import com.github.aidensuen.kafkatool.common.notify.NotificationService;
import com.github.aidensuen.kafkatool.compents.KafkaToolComponent;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorFactoryImpl;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactory.SERVICE;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Properties;

@Component
public class KafkaSettingsComponent implements KafkaToolComponent, DumbAware {

    private static final String SETTINGS_LABEL = "Settings";
    private KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent;
    private NotificationService notificationService;

    private JPanel mainPanel;
    private JPanel controlPanel;
    private JTextField bootstrapServersSettingField;
    private JTextField schemaRegistrySettingField;
    private JTextField avroPackagePrefixField;
    private JButton saveSettingsButton;
    private JButton restoreDefaultsButton;
    private EditorEx producerProperties;
    private EditorEx consumerProperties;

    @Autowired
    public KafkaSettingsComponent(KafkaToolPersistentStateComponent kafkaToolPersistentStateComponent, NotificationService notificationService) {
        this.kafkaToolPersistentStateComponent = kafkaToolPersistentStateComponent;
        this.notificationService = notificationService;
        this.initUI();
    }

    private void initUI() {
        this.mainPanel = new JPanel(new BorderLayout());

        controlPanel = new JPanel();

        controlPanel.setLayout(new GridLayout(6, 1, 0, 1));

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
        buttonJpanel.setLayout(new GridLayout(1, 2, 0, 1));

        buttonJpanel.add(this.saveSettingsButton);
        buttonJpanel.add(this.restoreDefaultsButton);

        controlPanel.setPreferredSize(new Dimension(mainPanel.getWidth(), 200));
        this.mainPanel.add(controlPanel, BorderLayout.NORTH);
        this.mainPanel.add(buttonJpanel, BorderLayout.SOUTH);
    }

    @Override
    public Content getContent(@NotNull Project project) {

        JPanel propertiesPanel = new JPanel(new GridLayout(2, 1, 1, 0));

        JPanel producerPanel = new JPanel(new BorderLayout());
        producerPanel.setBorder(BorderFactory.createTitledBorder("Producer Properties"));

        JPanel consumerPanel = new JPanel(new BorderLayout());
        consumerPanel.setBorder(BorderFactory.createTitledBorder("Consumer Properties"));


        EditorHighlighterFactory editorHighlighterFactory = EditorHighlighterFactory.getInstance();
        SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(JsonFileType.INSTANCE, project, (VirtualFile) null);
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        EditorFactory factory = EditorFactory.getInstance();
        Document schemaDocument = ((EditorFactoryImpl) factory).createDocument("", false, true);
        this.producerProperties = (EditorEx) factory.createEditor(schemaDocument, project);
        this.producerProperties.setHighlighter(editorHighlighterFactory.createEditorHighlighter(syntaxHighlighter, globalScheme));
        this.producerProperties.setCaretVisible(true);
        this.producerProperties.setCaretEnabled(true);
        this.producerProperties.setViewer(false);
        this.producerProperties.getFoldingModel().setFoldingEnabled(true);
        EditorSettings settings = this.producerProperties.getSettings();
        settings.setAutoCodeFoldingEnabled(true);
        settings.setFoldingOutlineShown(true);
        settings.setTabSize(4);
        settings.setAutoCodeFoldingEnabled(true);
        producerPanel.add(this.producerProperties.getComponent(), BorderLayout.CENTER);
        propertiesPanel.add(producerPanel);

        Document payloadDocument = ((EditorFactoryImpl) factory).createDocument("", false, true);
        this.consumerProperties = (EditorEx) factory.createEditor(payloadDocument, project);
        this.consumerProperties.setHighlighter(editorHighlighterFactory.createEditorHighlighter(syntaxHighlighter, globalScheme));
        this.consumerProperties.setCaretVisible(true);
        this.consumerProperties.setCaretEnabled(true);
        this.consumerProperties.setViewer(false);
        this.consumerProperties.getFoldingModel().setFoldingEnabled(true);
        consumerPanel.add(this.consumerProperties.getComponent(), BorderLayout.CENTER);
        propertiesPanel.add(consumerPanel);

        this.mainPanel.add(propertiesPanel, BorderLayout.CENTER);

        this.bootstrapServersSettingField.setText(this.kafkaToolPersistentStateComponent.getBootstrapServers());
        this.schemaRegistrySettingField.setText(this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl());
        this.avroPackagePrefixField.setText(this.kafkaToolPersistentStateComponent.getAvroPackagePrefix());
        this.avroPackagePrefixField.setText(this.kafkaToolPersistentStateComponent.getAvroPackagePrefix());
        this.saveSettingsButton.addActionListener((e) -> {
            this.kafkaToolPersistentStateComponent.setBootstrapServers(this.bootstrapServersSettingField.getText());
            this.kafkaToolPersistentStateComponent.setSchemaRegistryUrl(this.schemaRegistrySettingField.getText());
            this.kafkaToolPersistentStateComponent.setAvroPackagePrefix(this.avroPackagePrefixField.getText());
            this.resolveProperties();
            this.notificationService.success("Saved");
        });
        this.restoreDefaultsButton.addActionListener((event) -> {
            this.kafkaToolPersistentStateComponent.refresh();
            this.schemaRegistrySettingField.setText("http://localhost:8081");
            this.bootstrapServersSettingField.setText("localhost:9091");
            this.avroPackagePrefixField.setText("com.example");
            this.producerProperties.getDocument().setText("");
            this.consumerProperties.getDocument().setText("");
        });

        ContentFactory contentFactory = SERVICE.getInstance();
        return contentFactory.createContent(this.mainPanel, SETTINGS_LABEL, false);
    }

    private void resolveProperties() {
        InputStreamResource produderInputStreamResource = new InputStreamResource(new ByteArrayInputStream(this.producerProperties.getDocument().getText().getBytes()));
        InputStreamResource consumerInputStreamResource = new InputStreamResource(new ByteArrayInputStream(this.consumerProperties.getDocument().getText().getBytes()));
        try {
            PropertiesPropertySource produderResource = new ResourcePropertySource(produderInputStreamResource);
            PropertiesPropertySource consumerResource = new ResourcePropertySource(consumerInputStreamResource);
            Map iproduderProperties = produderResource.getSource();
            Map iconsumerProperties = consumerResource.getSource();
            iproduderProperties.remove("bootstrap.servers");
            iproduderProperties.remove("value.serializer");
            iproduderProperties.remove("key.serializer");
            iproduderProperties.remove("schema.registry.url");

            iconsumerProperties.remove("bootstrap.servers");
            iconsumerProperties.remove("value.deserializer");
            iconsumerProperties.remove("key.deserializer");
            iconsumerProperties.remove("schema.registry.url");
            kafkaToolPersistentStateComponent.setProducerProperties((Properties) iproduderProperties);
            kafkaToolPersistentStateComponent.setConsumerProperties((Properties) iconsumerProperties);
        } catch (Exception e) {
            this.notificationService.error(e.getMessage());
        }
    }
}
