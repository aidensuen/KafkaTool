package com.github.aidensuen.kafkatool.compents.producer.service.impl;


import com.github.aidensuen.kafkatool.common.exception.KafkaToolException;
import com.github.aidensuen.kafkatool.common.notify.NotificationService;
import com.github.aidensuen.kafkatool.compents.producer.service.AvroClassScanner;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AvroClassScannerImpl implements AvroClassScanner {

    private static final String JAR_URL_PREFIX = "jar:";
    private static final String FILE_URL_PREFIX = "file:/";
    private static final String DOLLAR_SIGN_CHARACTER = "$";
    private static final String ANNOYING_CHARACTERS = "!/";
    private static final String EMPTY_STRING = "";

    @Autowired
    private NotificationService notificationService;

    private ResourceLoader resourceLoader = new PathMatchingResourcePatternResolver();

    public Set<Class> loadAvroClassesFromProject(Project project, String packageName) {
        Objects.requireNonNull(project);
        Objects.requireNonNull(packageName);

        Module[] modules = ModuleManager.getInstance(project).getModules();

        List<? extends Class> avroClassListResult = Arrays.stream(modules).map((module) -> {
            return this.getAvroClassesFromModule(module, packageName);
        }).flatMap(Collection::stream).collect(Collectors.toList());
        return Sets.newHashSet(avroClassListResult);
    }

    private List<? extends Class> getAvroClassesFromModule(Module module, String packageName) {
        ModuleRootManager instance = ModuleRootManager.getInstance(module);

        List<String> entries = Arrays.stream(instance.getOrderEntries()).map(orderEntry -> {
            return Arrays.asList(orderEntry.getUrls(OrderRootType.CLASSES));
        }).flatMap(Collection::stream).collect(Collectors.toList());

        URL[] urls = entries.stream().map(url -> {
            return url.replace(JAR_URL_PREFIX, FILE_URL_PREFIX).replace(ANNOYING_CHARACTERS, EMPTY_STRING);
        }).map((url) -> {
            try {
                return new URL(url);
            } catch (MalformedURLException var2) {
                throw new KafkaToolException("Failed to transform url", var2);
            }
        }).toArray((x) -> {
            return new URL[x];
        });
        Set<URL> urlList = new HashSet<>();
        try {
            String searchPath = "file:" + module.getModuleFilePath().replaceAll(module.getName() + "\\.iml", "")
                    .replaceAll("\\\\", "/")
                    .replaceAll("\\.idea.*", "")
                    + "**/*.jar";
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(searchPath);
            for (Resource re : resources) {
                if (isCurrentModuleJar(module, re)) {
                    urlList.add(re.getURL());
                }
            }
        } catch (Exception var9) {
            this.notificationService.warning("Error occurred during avro class extraction " + NestedExceptionUtils.getRootCause(var9).getMessage());
        }

        urlList.addAll(Arrays.asList(urls));

        URLClassLoader urlClassLoader = new URLClassLoader(urlList.toArray(new URL[urlList.size()]));

        try {
            ClassPath classPath = ClassPath.from(urlClassLoader);
            Set<ClassPath.ClassInfo> topLevelClasses = classPath.getAllClasses();

            return topLevelClasses.parallelStream().filter(Objects::nonNull).filter((classInfo) -> {
                return classInfo.getName().contains(packageName);
            }).filter((classInfo) -> {
                return !classInfo.getName().contains(DOLLAR_SIGN_CHARACTER);
            }).map(ClassPath.ClassInfo::load).filter((aClass) -> {
                return Objects.nonNull(aClass.getSuperclass());
            }).filter((aClass) -> {
                return aClass.getSuperclass().getCanonicalName().equals(SpecificRecordBase.class.getCanonicalName());
            }).collect(Collectors.toList());
        } catch (Throwable var9) {
            this.notificationService.warning("Error occurred during avro class extraction " + NestedExceptionUtils.getRootCause(var9).getMessage());
        }
        return new ArrayList<>();
    }

    private boolean isCurrentModuleJar(Module module, Resource resource) throws IOException {
        String root = module.getModuleFilePath().replaceAll(module.getName() + "\\.iml", "")
                .replaceAll("\\\\", "/")
                .replaceAll("\\.idea.*", "");
        String targetDirectory = root + "target/";
        String gradleDirectory = root + "build/";
        String sourcePath = resource.getFile().getPath().replaceAll("\\\\", "/");
        return sourcePath.startsWith(targetDirectory) || sourcePath.startsWith(gradleDirectory);
    }

}