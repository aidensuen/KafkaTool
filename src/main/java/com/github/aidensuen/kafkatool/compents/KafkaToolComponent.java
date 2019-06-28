package com.github.aidensuen.kafkatool.compents;

import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

public interface KafkaToolComponent {
    Content getContent(@NotNull Project project);
}
