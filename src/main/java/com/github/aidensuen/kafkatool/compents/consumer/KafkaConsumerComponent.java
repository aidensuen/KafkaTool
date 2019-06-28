package com.github.aidensuen.kafkatool.compents.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.aidensuen.kafkatool.common.service.KafkaManagerService;
import com.github.aidensuen.kafkatool.compents.KafkaToolComponent;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
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
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactory.SERVICE;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaConsumerComponent implements KafkaToolComponent, DumbAware {

    private final String deserializer;
    private final String topic;
    private final Map<String, ConsumerRecord<String, Object>> consumerResults;
    private final KafkaManagerService kafkaManagerService;

    private ObjectMapper objectMapper;

    private JPanel consumerPanel;
    private JToolBar viewerToolbar;
    private JButton refreshButton;
    private JPanel consumerRecordViewer;
    private JList<String> consumerRecordList;
    private DefaultListModel<String> consumerRecordListModel;
    private JTabbedPane consumerChildTabs;
    private JScrollPane consumerRecordListScrollPane;
    private JButton clearButton;
    private JCheckBox clearOnPollCheckbox;
    private EditorEx responseViewer;

    public KafkaConsumerComponent(KafkaManagerService kafkaManagerService, ObjectMapper objectMapper, String deserializer, String topic) {
        this.kafkaManagerService = kafkaManagerService;
        this.objectMapper = objectMapper;
        this.deserializer = deserializer;
        this.topic = topic;
        this.consumerResults = new ConcurrentHashMap();
        this.initUI();
    }

    private void initUI() {
        consumerPanel = new JPanel();
        consumerPanel.setLayout(new BorderLayout());

        viewerToolbar = new JToolBar();
        viewerToolbar.setBorder(BorderFactory.createTitledBorder("Viewer"));

        refreshButton = new JButton("Poll");
        viewerToolbar.add(refreshButton);

        clearButton = new JButton("Clear");
        viewerToolbar.add(clearButton);

        clearOnPollCheckbox = new JCheckBox();
        clearOnPollCheckbox.setText("Clear if poll successfully");
        viewerToolbar.add(clearOnPollCheckbox);

        consumerPanel.add(viewerToolbar, BorderLayout.NORTH);

        consumerRecordList = new JBList();

        consumerRecordViewer = new JPanel();
        consumerRecordViewer.setLayout(new GridLayout(1, 2));


        consumerRecordListScrollPane = new JBScrollPane(consumerRecordList);

        consumerRecordViewer.add(consumerRecordListScrollPane);

        consumerPanel.add(consumerRecordViewer, BorderLayout.CENTER);

        consumerChildTabs = new JBTabbedPane();
        consumerChildTabs.addTab("Viewer", consumerPanel);

    }


    @Override
    public Content getContent(@NotNull Project project) {

        this.consumerRecordListModel = new DefaultListModel();
        this.consumerRecordList.setModel(this.consumerRecordListModel);
        EditorHighlighterFactory editorHighlighterFactory = EditorHighlighterFactory.getInstance();
        SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(JsonFileType.INSTANCE, project, null);
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        EditorFactory factory = EditorFactory.getInstance();
        final Document consumerResultsDocument = ((EditorFactoryImpl) factory).createDocument("Consumer Results\n", false, true);
        consumerResultsDocument.setReadOnly(false);
        this.responseViewer = (EditorEx) factory.createEditor(consumerResultsDocument, project);
        this.responseViewer.setHighlighter(editorHighlighterFactory.createEditorHighlighter(syntaxHighlighter, globalScheme));
        this.responseViewer.setCaretVisible(true);
        this.responseViewer.setCaretEnabled(true);
        this.responseViewer.setViewer(false);
        this.responseViewer.getFoldingModel().setFoldingEnabled(true);
        EditorSettings settings = this.responseViewer.getSettings();
        settings.setAutoCodeFoldingEnabled(true);
        settings.setFoldingOutlineShown(true);
        settings.setTabSize(4);
        settings.setAutoCodeFoldingEnabled(true);
        this.consumerRecordViewer.add(this.responseViewer.getComponent());
        this.refreshButton.setCursor(new Cursor(12));
        this.refreshButton.addActionListener((e) -> {
            ApplicationManager.getApplication().invokeLater(() -> {
                ApplicationManager.getApplication().runReadAction(() -> {
                    kafkaManagerService.consume(this.deserializer, this.topic, 100, 1, (optionalConsumerRecords) -> {
                        SwingUtilities.invokeLater(() -> {
                            optionalConsumerRecords.ifPresent((consumerRecords) -> {
                                if (this.clearOnPollCheckbox.isSelected()) {
                                    this.clearResults();
                                }
                                consumerRecords.forEach((consumerRecord) -> {
                                    String key = this.getConsumerRecordKey(consumerRecord);
                                    this.consumerResults.put(key, consumerRecord);
                                    this.consumerRecordListModel.addElement(key);
                                });
                            });
                        });
                    });
                });
            }, ModalityState.NON_MODAL);
        });
        this.clearButton.addActionListener((event) -> {
            this.clearResults();
        });
        this.consumerRecordList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList list = (JList) mouseEvent.getSource();
                String selectedValue = String.valueOf(list.getSelectedValue());
                ConsumerRecord<String, Object> selectedRecord = KafkaConsumerComponent.this.consumerResults.get(selectedValue);
                consumerResultsDocument.setText(KafkaConsumerComponent.this.formatText(selectedRecord));
            }
        });

        ContentFactory contentFactory = SERVICE.getInstance();
        return contentFactory.createContent(this.consumerPanel, "Consumer", false);
    }

    private void clearResults() {
        this.consumerRecordListModel.clear();
        this.consumerResults.clear();
    }

    private String getConsumerRecordKey(ConsumerRecord<String, Object> consumerRecord) {
        return String.format("%s:%s:%s", consumerRecord.partition(), consumerRecord.offset(), consumerRecord.key());
    }

    private String formatText(ConsumerRecord<String, Object> consumerRecord) {
        String prettyValue;
        try {
            JsonNode jsonNode = this.objectMapper.readTree(String.valueOf(consumerRecord.value()));
            prettyValue = this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (IOException e) {
            prettyValue = String.valueOf(consumerRecord.value());
        }

        return String.format("Topic: %s\nPartition: %s\nOffset: %s\nTimestamp: %s\nHeaders: %s\nKey: %s\nBody: %s\n", consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), consumerRecord.timestamp(), Arrays.toString(consumerRecord.headers().toArray()), consumerRecord.key(), prettyValue);
    }

}
