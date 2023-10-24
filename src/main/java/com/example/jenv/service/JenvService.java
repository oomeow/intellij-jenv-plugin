package com.example.jenv.service;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.JenvState;
import com.example.jenv.constant.JenvConstants;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class JenvService {
    public void initProject(Project project) {
        String userHomePath = System.getProperty("user.home");
        String jenvFilePath = userHomePath + File.separator + JenvConstants.JENV_FILE_EXTENSION.getName();
        VirtualFile jenvFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jenvFilePath));
        JenvState state = Objects.requireNonNull(JenvStateService.getInstance().getState());

        if (jenvFile != null && jenvFile.exists()) {
            state.setJenvInstalled(true);
        }
        if (CollectionUtils.isNotEmpty(JenvHelper.getAllJdkVersionList())) {
            state.setJavaInstalled(true);
        }
        String jEnvVersionPath = userHomePath + File.separator + ".jenv/versions";
        VirtualFile jenvVersionDir = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jEnvVersionPath));
        if (jenvVersionDir != null && jenvVersionDir.exists()) {
            VirtualFile[] children = jenvVersionDir.getChildren();
            for (VirtualFile jdkVersionDir : children) {
                System.out.println(jdkVersionDir.getPath());
                System.out.println(jdkVersionDir.getCanonicalPath());
            }
        }

        String projectJdkVersionFilePath = project.getBasePath() + File.separator + JenvConstants.VERSION_FILE.getName();
        VirtualFile projectJenvFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(projectJdkVersionFilePath));

        if (projectJenvFile != null && projectJenvFile.exists()) {
            state.setProjectJenvExists(true);
            state.setProjectJenvFilePath(projectJenvFile.getPath());
            try {
                Path path = Paths.get(projectJenvFile.getPath());
                String jdkVersion = Files.readAllLines(path).get(0).trim();
                state.setCurrentJavaVersion(jdkVersion);
                Sdk jdk = ProjectJdkTable.getInstance().findJdk(state.getFormattedJavaVersion());
                if (jdk != null) {
                    SdkConfigurationUtil.setDirectoryProjectSdk(project, jdk);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        state.setProjectOpened(true);
    }

    public void changeJenvVersion() {
        JenvState state = Objects.requireNonNull(JenvStateService.getInstance().getState());
        Sdk jdk = ProjectJdkTable.getInstance().findJdk(state.getFormattedJavaVersion());
        SdkConfigurationUtil.setDirectoryProjectSdk(state.getProject(), jdk);
        if (state.isChangeJenvByDialog()) {
            VirtualFile fileByNioPath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(state.getProjectJenvFilePath()));
            if (fileByNioPath != null && fileByNioPath.exists()) {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        fileByNioPath.setBinaryContent(state.getJenvJavaVersion().getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                });
            }
        }
    }
}
