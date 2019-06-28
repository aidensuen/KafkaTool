package com.github.aidensuen.kafkatool;

import com.github.aidensuen.kafkatool.compents.consumer.KafkaConsumerContainerComponent;
import com.github.aidensuen.kafkatool.compents.manager.KafkaManagerComponent;
import com.github.aidensuen.kafkatool.compents.producer.KafkaProducerComponent;
import com.github.aidensuen.kafkatool.compents.settings.KafkaSettingsComponent;
import com.github.aidensuen.kafkatool.config.AppConfig;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Objects;

public class KafkaToolWindow implements ToolWindowFactory, DumbAware {

    private AnnotationConfigApplicationContext context;

    @Override
    public void init(ToolWindow window) {
        context = new AnnotationConfigApplicationContext();
        context.setClassLoader(AppConfig.class.getClassLoader());
        context.register(AppConfig.class);
        context.refresh();
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = Objects.requireNonNull(toolWindow).getContentManager();
        KafkaProducerComponent kafkaProducerComponent = context.getBean("kafkaProducerComponent", KafkaProducerComponent.class);
        contentManager.addContent(kafkaProducerComponent.getContent(project));
        KafkaConsumerContainerComponent kafkaConsumerContainerComponent = context.getBean("kafkaConsumerContainerComponent", KafkaConsumerContainerComponent.class);
        contentManager.addContent(kafkaConsumerContainerComponent.getContent(project));
        KafkaManagerComponent kafkaManagerComponent = context.getBean("kafkaManagerComponent", KafkaManagerComponent.class);
        contentManager.addContent(kafkaManagerComponent.getContent(project));
        KafkaSettingsComponent kafkaSettingsComponent = context.getBean("kafkaSettingsComponent", KafkaSettingsComponent.class);
        contentManager.addContent(kafkaSettingsComponent.getContent(project));
    }
}
