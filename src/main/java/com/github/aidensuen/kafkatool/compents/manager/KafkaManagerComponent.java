package com.github.aidensuen.kafkatool.compents.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.aidensuen.kafkatool.common.service.KafkaManagerService;
import com.github.aidensuen.kafkatool.compents.KafkaToolComponent;
import com.github.aidensuen.kafkatool.model.SchemaVersion;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.apache.avro.Schema;
import org.apache.kafka.common.PartitionInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class KafkaManagerComponent implements KafkaToolComponent, DumbAware {

    private static final Map<String, List<SchemaVersion>> SCHEMA_MAP = new ConcurrentHashMap<>();
    private static final Map<String, List<PartitionInfo>> TOPIC_MAP = Maps.newHashMap();

    @Autowired
    private KafkaManagerService kafkaManagerService;

    private JTabbedPane tabbedPane;
    private JButton refreshTopicsButton;
    private JTree subjectTree;
    private JTextArea schemaTextArea;
    private JButton refreshSubjectsButton;
    private JTree topicTree;
    private JTextArea topicTextArea;
    private JButton deleteSchemaButton;
    private TreeModel topicsTreeModel;
    private DefaultMutableTreeNode topicsTreeNode;
    private String selectedTopic;
    private String selectedPartition;
    private TreeModel subjectsTreeModel;
    private DefaultMutableTreeNode subjectsTreeNode;
    private String selectedSubject;
    private String selectedVersion;

    public KafkaManagerComponent() {
        this.initUI();
    }

    private void initUI() {
        tabbedPane = new JBTabbedPane();

        JPanel topicsPanel = new JPanel();
        topicsPanel.setLayout(new BorderLayout());
        initTop(topicsPanel);
        initCenter(topicsPanel);

        JPanel schemaPanel = new JPanel();
        schemaPanel.setLayout(new BorderLayout());
        initSchema(schemaPanel);
        tabbedPane.addTab("Topics", topicsPanel);
        tabbedPane.addTab("Schema Registry", schemaPanel);

    }


    private void initTop(JPanel mainPanel) {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(1, 5));
        refreshTopicsButton = new JButton("Refresh");
        jPanel.add(refreshTopicsButton);

        JPanel parent = new JPanel();
        parent.setLayout(new BorderLayout());
        parent.add(jPanel, BorderLayout.WEST);

        mainPanel.add(parent, BorderLayout.NORTH);
    }

    private void initCenter(JPanel mainPanel) {

        JSplitPane jSplitPane = new JSplitPane();
        jSplitPane.setDividerLocation(0.4);
        jSplitPane.setDividerSize(5);

        topicTree = new Tree();
        topicTree.setShowsRootHandles(true);

        JScrollPane jPanel = new JBScrollPane(topicTree);
        jPanel.setPreferredSize(new Dimension(150, 20));

        JScrollPane scrollPane = new JBScrollPane(initTopicTextArea());

        jSplitPane.add(jPanel, JSplitPane.LEFT);
        jSplitPane.add(scrollPane, JSplitPane.RIGHT);

        mainPanel.add(jSplitPane, BorderLayout.CENTER);
    }

    private JTextArea initTopicTextArea() {
        topicTextArea = new JTextArea();
        topicTextArea.setLineWrap(true);
        topicTextArea.setWrapStyleWord(true);
        return topicTextArea;
    }

    private void initSchema(JPanel mainPanel) {

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(1, 5));
        refreshSubjectsButton = new JButton("Refresh");
        jPanel.add(refreshSubjectsButton);

        JPanel parent = new JPanel();
        parent.setLayout(new BorderLayout());
        parent.add(jPanel, BorderLayout.WEST);

        mainPanel.add(parent, BorderLayout.NORTH);

        JSplitPane jSplitPane = new JSplitPane();
        jSplitPane.setDividerLocation(0.4);
        jSplitPane.setDividerSize(5);

        subjectTree = new Tree();
        subjectTree.setShowsRootHandles(true);

        JScrollPane jScrollPane = new JBScrollPane(subjectTree);
        jScrollPane.setPreferredSize(new Dimension(150, 20));

        schemaTextArea = new JTextArea();
        schemaTextArea.setLineWrap(true);
        schemaTextArea.setWrapStyleWord(true);

        JPanel rigthPanel = new JPanel();
        rigthPanel.setLayout(new BorderLayout());

        deleteSchemaButton = new JButton("Delete Schema");

        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(new GridLayout(1, 5));
        jPanel1.add(deleteSchemaButton);

        JPanel parent1 = new JPanel();
        parent1.setLayout(new BorderLayout());
        parent1.add(jPanel1, BorderLayout.WEST);

        rigthPanel.add(parent1, BorderLayout.NORTH);

        JTabbedPane jTabbedPane = new JBTabbedPane();

        JScrollPane scrollPane = new JBScrollPane(schemaTextArea);
        jTabbedPane.addTab("View", scrollPane);

        rigthPanel.add(jTabbedPane, BorderLayout.CENTER);

        jSplitPane.add(jScrollPane, JSplitPane.LEFT);
        jSplitPane.add(rigthPanel, JSplitPane.RIGHT);

        mainPanel.add(jSplitPane, BorderLayout.CENTER);

    }

    @Override
    public Content getContent(@NotNull Project project) {

        this.refreshTopicsButton.setCursor(new Cursor(12));
        this.topicsTreeNode = new DefaultMutableTreeNode("Topics");
        this.topicsTreeModel = new DefaultTreeModel(this.topicsTreeNode);
        this.topicTree.setModel(this.topicsTreeModel);
        this.topicTree.setCursor(new Cursor(12));
        this.refreshTopicsButton.setCursor(new Cursor(12));

        this.refreshTopicsButton.addActionListener((actionEvent) -> {
            this.topicsTreeNode.removeAllChildren();
            this.kafkaManagerService.getDetailedTopicList((topicList) -> {
                SwingUtilities.invokeLater(() -> {
                    topicList.forEach((topic, partitionInfoList) -> {
                        DefaultMutableTreeNode topicTreeNode = new DefaultMutableTreeNode(topic);
                        partitionInfoList.forEach((partitionInfo) -> {
                            DefaultMutableTreeNode partitionTreeNode = new DefaultMutableTreeNode(String.valueOf(partitionInfo.partition()));
                            topicTreeNode.add(partitionTreeNode);
                        });
                        TOPIC_MAP.put(topic, partitionInfoList);
                        this.topicsTreeNode.add(topicTreeNode);
                    });
                });
                this.topicTree.updateUI();
            });
        });
        this.topicTree.addTreeSelectionListener((actionEvent) -> {
            SwingUtilities.invokeLater(() -> {
                TreePath newLeadSelectionPath = actionEvent.getNewLeadSelectionPath();
                if (newLeadSelectionPath.getPathCount() > 1) {
                    this.selectedTopic = String.valueOf(newLeadSelectionPath.getPath()[1]);
                    if (newLeadSelectionPath.getPathCount() == 2) {
                        this.selectedPartition = "";
                        this.topicTextArea.setText((TOPIC_MAP.get(this.selectedTopic)).stream().map(t -> t.toString()).collect(Collectors.joining("\n")));
                    } else if (newLeadSelectionPath.getPathCount() == 3) {
                        this.selectedPartition = String.valueOf(newLeadSelectionPath.getPath()[2]);
                        this.topicTextArea.setText((TOPIC_MAP.get(this.selectedTopic)).stream().filter((partitionInfo) -> {
                            return String.valueOf(partitionInfo.partition()).equals(this.selectedPartition);
                        }).map(PartitionInfo::toString).findFirst().orElse(""));
                    }
                }
            });
        });

        this.subjectsTreeNode = new DefaultMutableTreeNode("Subjects");
        this.subjectsTreeModel = new DefaultTreeModel(this.subjectsTreeNode);
        this.subjectTree.setModel(this.subjectsTreeModel);
        this.subjectTree.setCursor(new Cursor(12));
        this.refreshSubjectsButton.setCursor(new Cursor(12));
        this.refreshSubjectsButton.addActionListener(this::refreshSchemas);
        this.subjectTree.addTreeSelectionListener((e) -> {
            TreePath newLeadSelectionPath = e.getNewLeadSelectionPath();
            if (newLeadSelectionPath.getPathCount() > 1) {
                this.selectedSubject = String.valueOf(newLeadSelectionPath.getPath()[1]);
                if (newLeadSelectionPath.getPathCount() == 2) {
                    this.deleteSchemaButton.setEnabled(false);
                    this.selectedVersion = "";
                    this.schemaTextArea.setText("");
                } else if (newLeadSelectionPath.getPathCount() == 3) {
                    this.deleteSchemaButton.setEnabled(true);
                    this.selectedVersion = String.valueOf(newLeadSelectionPath.getPath()[2]);
                    String selectedSchema = (SCHEMA_MAP.get(this.selectedSubject)).stream().filter((schemaVersion) -> {
                        return schemaVersion.getVersion().equals(this.selectedVersion);
                    }).map((schemaVersion) -> {
                        return (new Schema.Parser()).parse(schemaVersion.getSchema()).toString(true);
                    }).findFirst().orElse("");
                    this.schemaTextArea.setText(selectedSchema);
                }
            }

        });
        this.deleteSchemaButton.addActionListener((actionEvent) -> {
            if (!Strings.isNullOrEmpty(this.selectedVersion)) {
                this.kafkaManagerService.deleteSchema(this.selectedSubject, this.selectedVersion, (result) -> {
                    if (result) {
                        this.refreshSchemas(actionEvent);
                        this.schemaTextArea.setText("");
                    }
                });
            }

        });

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        return contentFactory.createContent(this.tabbedPane, "Manager", false);
    }

    private void refreshSchemas(ActionEvent actionEvent) {
        this.subjectsTreeNode.removeAllChildren();
        this.kafkaManagerService.listSubjects(subjects -> {
            SwingUtilities.invokeLater(() -> {
                subjects.forEach((subject) -> {
                    DefaultMutableTreeNode subjectTreeNode = new DefaultMutableTreeNode(subject.getSubjectName());
                    SCHEMA_MAP.put(subject.getSubjectName(), subject.getSchemaVersionList());
                    subject.getSchemaVersionList().forEach((schemaVersion) -> {
                        DefaultMutableTreeNode versionTreeNode = new DefaultMutableTreeNode(schemaVersion.getVersion());
                        subjectTreeNode.add(versionTreeNode);
                    });
                    this.subjectsTreeNode.add(subjectTreeNode);
                });
                this.subjectTree.updateUI();
            });
        });

    }

    public KafkaManagerService getKafkaManagerService() {
        return kafkaManagerService;
    }

    public void setKafkaManagerService(KafkaManagerService kafkaManagerService) {
        this.kafkaManagerService = kafkaManagerService;
    }
}
