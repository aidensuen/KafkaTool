package com.github.aidensuen.kafkatool.compents.settings;

import com.github.aidensuen.kafkatool.common.KafkaToolPersistentStateComponent;
import com.github.aidensuen.kafkatool.common.notify.NotificationService;
import com.github.aidensuen.kafkatool.common.service.KafkaManagerService;
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
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactory.SERVICE;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLFileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private EditorEx kafkaProperties;

    @Autowired
    private KafkaManagerService kafkaManagerService;

    @Autowired
    private ApplicationContext context;

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

        JPanel propertiesPanel = new JPanel(new GridLayout(1, 1, 1, 0));

        JPanel producerPanel = new JPanel(new BorderLayout());
        producerPanel.setBorder(BorderFactory.createTitledBorder("Kafka Properties"));

        EditorHighlighterFactory editorHighlighterFactory = EditorHighlighterFactory.getInstance();
        SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(YAMLFileType.YML, project, null);
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        EditorFactory factory = EditorFactory.getInstance();
        Document schemaDocument = ((EditorFactoryImpl) factory).createDocument("", false, true);
        this.kafkaProperties = (EditorEx) factory.createEditor(schemaDocument, project);
        this.kafkaProperties.setHighlighter(editorHighlighterFactory.createEditorHighlighter(syntaxHighlighter, globalScheme));
        this.kafkaProperties.setCaretVisible(true);
        this.kafkaProperties.setCaretEnabled(true);
        this.kafkaProperties.setViewer(false);
        this.kafkaProperties.getFoldingModel().setFoldingEnabled(true);
        EditorSettings settings = this.kafkaProperties.getSettings();
        settings.setAutoCodeFoldingEnabled(true);
        settings.setFoldingOutlineShown(true);
        settings.setTabSize(4);
        settings.setAutoCodeFoldingEnabled(true);
        producerPanel.add(this.kafkaProperties.getComponent(), BorderLayout.CENTER);
        propertiesPanel.add(producerPanel);

        this.mainPanel.add(propertiesPanel, BorderLayout.CENTER);

        this.bootstrapServersSettingField.setText(this.kafkaToolPersistentStateComponent.getBootstrapServers());
        this.schemaRegistrySettingField.setText(this.kafkaToolPersistentStateComponent.getSchemaRegistryUrl());
        this.avroPackagePrefixField.setText(this.kafkaToolPersistentStateComponent.getAvroPackagePrefix());
        this.avroPackagePrefixField.setText(this.kafkaToolPersistentStateComponent.getAvroPackagePrefix());
        this.saveSettingsButton.addActionListener((e) -> {
            this.kafkaToolPersistentStateComponent.refresh();
            this.kafkaManagerService.refresh();
            this.kafkaToolPersistentStateComponent.setBootstrapServers(this.bootstrapServersSettingField.getText());
            this.kafkaToolPersistentStateComponent.setSchemaRegistryUrl(this.schemaRegistrySettingField.getText());
            this.kafkaToolPersistentStateComponent.setAvroPackagePrefix(this.avroPackagePrefixField.getText());
            this.resolveProperties();
            this.notificationService.success("Saved");
        });
        this.restoreDefaultsButton.addActionListener((event) -> {
            this.kafkaToolPersistentStateComponent.refresh();
            this.kafkaManagerService.refresh();
            this.schemaRegistrySettingField.setText("http://localhost:8081");
            this.bootstrapServersSettingField.setText("localhost:9091");
            this.avroPackagePrefixField.setText("com.example");
            this.kafkaProperties.getDocument().setText("");
        });

        ContentFactory contentFactory = SERVICE.getInstance();
        return contentFactory.createContent(this.mainPanel, SETTINGS_LABEL, false);
    }

    private synchronized void resolveProperties() {
        try {
            InputStreamResource kafkaPropertiesInputStreamResource = new InputStreamResource(new ByteArrayInputStream(this.kafkaProperties.getDocument().getText().getBytes()));

            YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
            List<PropertySource<?>> propertySources = yamlPropertySourceLoader.load("kafkaProperties", kafkaPropertiesInputStreamResource);

            MutablePropertySources mutablePropertySources = ((StandardEnvironment) this.context.getEnvironment()).getPropertySources();
            propertySources.forEach(propertySource -> {
                if (mutablePropertySources.contains(propertySource.getName())) {
                    mutablePropertySources.replace(propertySource.getName(), propertySource);
                } else {
                    mutablePropertySources.addFirst(propertySource);
                }
            });
            Field refreshedField = ReflectionUtils.findField(this.context.getClass(), "refreshed");
            ReflectionUtils.makeAccessible(refreshedField);
            AtomicBoolean refreshed = (AtomicBoolean) ReflectionUtils.getField(refreshedField, this.context);
            refreshed.set(false);
            ((AnnotationConfigApplicationContext) this.context).close();
            ((AnnotationConfigApplicationContext) this.context).refresh();
        } catch (Exception e) {
            this.notificationService.error(e.getMessage());
        }
    }
}
