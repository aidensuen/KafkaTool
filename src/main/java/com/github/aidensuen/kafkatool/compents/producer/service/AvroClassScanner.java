package com.github.aidensuen.kafkatool.compents.producer.service;

import com.intellij.openapi.project.Project;

import java.util.Set;

public interface AvroClassScanner {
    Set<Class> loadAvroClassesFromProject(Project var1, String packageName);
}
