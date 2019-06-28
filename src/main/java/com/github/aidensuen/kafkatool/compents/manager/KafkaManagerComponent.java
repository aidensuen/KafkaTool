package com.github.aidensuen.kafkatool.compents.manager;

import com.github.aidensuen.kafkatool.common.service.KafkaManagerService;
import com.github.aidensuen.kafkatool.compents.KafkaToolComponent;
import com.github.aidensuen.kafkatool.model.SchemaVersion;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class KafkaManagerComponent implements KafkaToolComponent, DumbAware {

    private static final Map<String, List<SchemaVersion>> SCHEMA_MAP =new ConcurrentHashMap<>();
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

         tabbedPane.addTab("Topics", topicsPanel);

    }

    private void initTop(JPanel mainPanel){
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(1, 5));
        refreshTopicsButton = new JButton("Refresh");
        jPanel.add(refreshTopicsButton);

        JPanel parent = new JPanel();
        parent.setLayout(new BorderLayout());
        parent.add(jPanel, BorderLayout.WEST);

        mainPanel.add(parent, BorderLayout.NORTH);
    }

    private void initCenter(JPanel mainPanel){

        JSplitPane jSplitPane = new JSplitPane();
        jSplitPane.setDividerLocation(0.4);
        jSplitPane.setDividerSize(5);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(1,1));
        jPanel.setPreferredSize(new Dimension(100, 20));
        initTopics(jPanel);

        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(new GridLayout(1, 1));
        initTopicTextArea(jPanel1);

        jSplitPane.add(jPanel, JSplitPane.LEFT);
        jSplitPane.add(jPanel1, JSplitPane.RIGHT);

        mainPanel.add(jSplitPane, BorderLayout.CENTER);
    }
    private void initTopics(JPanel mainPanel){
        topicTree = new Tree();
        mainPanel.add(topicTree);
    }

    private void initTopicTextArea(JPanel mainPanel){
        topicTextArea = new JTextArea();
        topicTextArea.setLineWrap(true);
        topicTextArea.setWrapStyleWord(true);
        mainPanel.add(topicTextArea);
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
                        this.topicTextArea.setText((String)((java.util.List)TOPIC_MAP.get(this.selectedTopic)).stream().map(t->t.toString()).collect(Collectors.joining()));
                    } else if (newLeadSelectionPath.getPathCount() == 3) {
                        this.selectedPartition = String.valueOf(newLeadSelectionPath.getPath()[2]);
                        this.topicTextArea.setText((TOPIC_MAP.get(this.selectedTopic)).stream().filter((partitionInfo) -> {
                            return String.valueOf(partitionInfo.partition()).equals(this.selectedPartition);
                        }).map(PartitionInfo::toString).findFirst().orElse(""));
                    }
                }
            });
        });

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        return contentFactory.createContent(this.tabbedPane, "Manager", false);
    }

    public KafkaManagerService getKafkaManagerService() {
        return kafkaManagerService;
    }

    public void setKafkaManagerService(KafkaManagerService kafkaManagerService) {
        this.kafkaManagerService = kafkaManagerService;
    }
}
