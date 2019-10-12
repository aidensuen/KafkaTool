package com.github.aidensuen.kafkatool.compents.producer;

import com.github.aidensuen.kafkatool.compents.KafkaToolComponent;
import com.github.aidensuen.kafkatool.compents.producer.service.KafkaProducerService;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Set;

@Component
public class KafkaProducerComponent implements KafkaToolComponent, DumbAware {

    private final KafkaProducerService kafkaProducerService;

    private JTabbedPane tabbedPane;
    private JComboBox<String> topicComboBox;
    private JButton produceButton;
    private JComboBox<String> avroClassComboBox;
    private EditorEx schemaEditor;
    private EditorEx payloadEditor;
    private JTextField keyField;
    private JTextField selectedTopicField;
    private JTextField partitionField;
    private JComboBox<String> serializerComboBox;
    private JButton topicRefreshButton;
    private JButton refreshAvroClasses;
    private JPanel schemaPanel;
    private JPanel payloadPanel;
    private JPanel keyPanel;
    private JButton randomGeneratePayloadButton;

    @Autowired
    public KafkaProducerComponent(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
        this.initUI();
    }

    private void initUI() {
        initTabbedPane();
    }

    private void initTabbedPane() {
        tabbedPane = new JBTabbedPane();

        JPanel first = new JPanel();
        first.setLayout(new BorderLayout());
        initTopicPanel(first);
        initSchemaPanel(first);

        JPanel second = new JPanel();
        second.setLayout(new BorderLayout());
        initPayloadPanel(second);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(0.5);
        splitPane.setDividerSize(5);

        splitPane.add(first);
        splitPane.add(second);

        JPanel four = new JPanel();
        four.setLayout(new GridLayout(1, 1));
        initKeyPanel(four);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(splitPane, BorderLayout.CENTER);

        mainPanel.add(four, BorderLayout.SOUTH);

        tabbedPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                splitPane.setDividerLocation(0.5);
            }
        });

        tabbedPane.addTab("producer", mainPanel);
    }

    private void initTopicPanel(JPanel mainPanel) {
        JPanel topicPanel = new JPanel();
        GridLayout layout = new GridLayout(1, 3);
        topicPanel.setLayout(layout);
        topicPanel.setBorder(BorderFactory.createTitledBorder("Topic"));

        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(new GridLayout(1, 2));
        topicComboBox = new ComboBox<>();
        jPanel1.add(topicComboBox);
        topicRefreshButton = new JButton("Refresh");
        jPanel1.add(topicRefreshButton);

        selectedTopicField = new JTextField();
        selectedTopicField.setToolTipText("Topic");

        topicPanel.add(jPanel1);
        topicPanel.add(selectedTopicField);


        serializerComboBox = new ComboBox<>();
        topicPanel.add(serializerComboBox);

        mainPanel.add(topicPanel, BorderLayout.NORTH);
    }

    private void initSchemaPanel(JPanel mainPanel) {
        this.schemaPanel = new JPanel();
        schemaPanel.setLayout(new BorderLayout());
        schemaPanel.setBorder(BorderFactory.createTitledBorder("Schema"));

        JPanel jPanel1 = new JPanel();
        jPanel1.setBorder(BorderFactory.createTitledBorder("Avro Class Scanner"));
        jPanel1.setLayout(new BorderLayout());

        avroClassComboBox = new ComboBox<>();
        jPanel1.add(avroClassComboBox, BorderLayout.CENTER);

        refreshAvroClasses = new JButton("Refresh");
        jPanel1.add(refreshAvroClasses, BorderLayout.EAST);

        schemaPanel.add(jPanel1, BorderLayout.NORTH);

        mainPanel.add(schemaPanel, BorderLayout.CENTER);
    }

    private void initPayloadPanel(JPanel mainPanel) {
        this.payloadPanel = new JPanel();
        payloadPanel.setLayout(new BorderLayout());
        payloadPanel.setBorder(BorderFactory.createTitledBorder("Payload"));

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.setBorder(BorderFactory.createTitledBorder("Generate"));

        randomGeneratePayloadButton = new JButton("Random");
        jPanel.add(randomGeneratePayloadButton, BorderLayout.WEST);

        payloadPanel.add(jPanel, BorderLayout.NORTH);

        mainPanel.add(payloadPanel, BorderLayout.CENTER);
    }

    private void initKeyPanel(JPanel mainPanel) {
        keyPanel = new JPanel();
        keyPanel.setLayout(new BorderLayout());
        keyPanel.setBorder(BorderFactory.createTitledBorder("Key"));
        keyField = new JTextField();
        keyPanel.add(keyField, BorderLayout.CENTER);


        JPanel jLabel1 = new JPanel();
        jLabel1.setLayout(new BorderLayout());
        jLabel1.setBorder(BorderFactory.createTitledBorder("Partition"));
        partitionField = new JTextField();
        jLabel1.add(partitionField, BorderLayout.CENTER);

        JLabel jLabel = new JLabel();
        jLabel.setLayout(new GridLayout(1, 2));
        jLabel.setPreferredSize(new Dimension(mainPanel.getWidth(), 50));
        jLabel.add(keyPanel);
        jLabel.add(jLabel1);

        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());

        main.add(jLabel, BorderLayout.CENTER);
        produceButton = new JButton("Send");
        main.add(produceButton, BorderLayout.EAST);

        mainPanel.add(main);
    }


    @Override
    public Content getContent(@NotNull Project project) {
        this.topicComboBox.removeAllItems();
        this.avroClassComboBox.removeAllItems();
        Set<String> serializers = this.kafkaProducerService.getAllSerializers();
        JComboBox jComboBox = this.serializerComboBox;
        serializers.forEach(jComboBox::addItem);
        //topicBox listener
        this.topicComboBox.addItemListener((e) -> {
            if (e.getStateChange() == 1) {
                this.selectedTopicField.setText(String.valueOf(e.getItem()).trim());
            }

        });
        this.produceButton.setCursor(new Cursor(12));
        //producer listener
        this.produceButton.addActionListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                Integer partition = 0;
                try {
                    partition = Integer.valueOf(this.partitionField.getText());
                } catch (Exception r1) {
                    partition = 0;
                }
                this.kafkaProducerService.produceMessage(this.selectedTopicField.getText(), partition, String.valueOf(this.serializerComboBox.getSelectedItem()), this.schemaEditor.getDocument().getText().replaceAll("\\n", ""), this.keyField.getText(), this.payloadEditor.getDocument().getText().replaceAll("\\n", ""));
            });
        });
        //topicRefresh listener
        this.topicRefreshButton.addActionListener((e) -> {
            this.refreshTopicList();
        });
        this.topicRefreshButton.setCursor(new Cursor(12));

        //refreshAvroClasses listener
        this.refreshAvroClasses.addActionListener((e) -> {
            this.avroClassComboBox.removeAllItems();
            this.kafkaProducerService.refreshAvroClassList(project, avroClassList -> {
                SwingUtilities.invokeLater(() -> {
                    JComboBox jComboBox1 = this.avroClassComboBox;
                    avroClassList.forEach(jComboBox1::addItem);
                });
            });
        });
        this.refreshAvroClasses.setCursor(new Cursor(12));

        //avroClassComboBox listener
        this.avroClassComboBox.addItemListener((e) -> {
            if (e.getStateChange() == 1) {
                SwingUtilities.invokeLater(() -> {
                    String selectedAvroClass = String.valueOf(this.avroClassComboBox.getSelectedItem());
                    String avroSchema = this.kafkaProducerService.getAvroSchema(selectedAvroClass);
                    PsiFile newPsi = PsiFileFactory.getInstance(project).createFileFromText(JsonLanguage.INSTANCE, avroSchema);
                    this.schemaEditor.getDocument().setText(newPsi.getViewProvider().getDocument().getText());
                });
            }

        });
        EditorHighlighterFactory editorHighlighterFactory = EditorHighlighterFactory.getInstance();
        SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(JsonFileType.INSTANCE, project, null);
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        EditorFactory factory = EditorFactory.getInstance();
        Document schemaDocument = ((EditorFactoryImpl) factory).createDocument("", false, true);
        this.schemaEditor = (EditorEx) factory.createEditor(schemaDocument, project);
        this.schemaEditor.setHighlighter(editorHighlighterFactory.createEditorHighlighter(syntaxHighlighter, globalScheme));
        this.schemaEditor.setCaretVisible(true);
        this.schemaEditor.setCaretEnabled(true);
        this.schemaEditor.setViewer(false);
        this.schemaEditor.getFoldingModel().setFoldingEnabled(true);
        EditorSettings settings = this.schemaEditor.getSettings();
        settings.setAutoCodeFoldingEnabled(true);
        settings.setFoldingOutlineShown(true);
        settings.setTabSize(4);
        settings.setAutoCodeFoldingEnabled(true);
        this.schemaPanel.add(this.schemaEditor.getComponent(), BorderLayout.CENTER);

        Document payloadDocument = ((EditorFactoryImpl) factory).createDocument("", false, true);
        this.payloadEditor = (EditorEx) factory.createEditor(payloadDocument, project);
        this.payloadEditor.setHighlighter(editorHighlighterFactory.createEditorHighlighter(syntaxHighlighter, globalScheme));
        this.payloadEditor.setCaretVisible(true);
        this.payloadEditor.setCaretEnabled(true);
        this.payloadEditor.setViewer(false);
        this.payloadEditor.getFoldingModel().setFoldingEnabled(true);
        this.payloadPanel.add(this.payloadEditor.getComponent(), BorderLayout.CENTER);
        this.keyField.setPreferredSize(new Dimension(100, 1));
        this.randomGeneratePayloadButton.setCursor(new Cursor(12));

        //generate schema listener
        this.randomGeneratePayloadButton.addActionListener((e) -> {
            this.kafkaProducerService.generateRandomPayloadFromSchema(this.schemaEditor.getDocument().getText(), text -> {
                SwingUtilities.invokeLater(() -> {
                    this.payloadEditor.getDocument().setText(text.replaceAll("\r", ""));
                });
            });
        });


        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        return contentFactory.createContent(this.tabbedPane, "Producer", false);
    }

    private void refreshTopicList() {
        this.kafkaProducerService.getTopicList(topicList -> {
            SwingUtilities.invokeLater(() -> {
                this.topicComboBox.removeAllItems();
                topicList.forEach(this.topicComboBox::addItem);
            });
        });
    }
}
