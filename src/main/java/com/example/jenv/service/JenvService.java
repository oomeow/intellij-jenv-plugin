package com.example.jenv.service;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.ProjectJenvState;
import com.example.jenv.constant.JenvConstants;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class JenvService {

    public static JenvService getInstance() {
        return ApplicationManager.getApplication().getService(JenvService.class);
    }

    public void initProject(Project project) {
        String userHomePath = System.getProperty("user.home");
        String jenvFilePath = userHomePath + File.separator + JenvConstants.JENV_DIR;
        VirtualFile jenvFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jenvFilePath));
        JenvStateService jenvStateService = project.getService(JenvStateService.class);
        ProjectJenvState state = Objects.requireNonNull(jenvStateService.getState());

        if (jenvFile != null && jenvFile.exists()) {
            state.setJenvInstalled(true);
        }
        if (CollectionUtils.isNotEmpty(JenvHelper.getAllIdeaJdkVersionList())) {
            state.setJavaInstalled(true);
        }
        JenvHelper.findAllJenvJdkHomePath();

        String projectJdkVersionFilePath = project.getBasePath() + File.separator + JenvConstants.VERSION_FILE;
        VirtualFile projectJenvFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(projectJdkVersionFilePath));
        if (projectJenvFile != null && projectJenvFile.exists()) {
            state.setProjectJenvExists(true);
            state.setProjectJenvFilePath(projectJenvFile.getPath());
            try {
                Path path = Paths.get(projectJenvFile.getPath());
                String jdkVersion = Files.readString(path).trim();
                state.setCurrentJavaVersion(jdkVersion);
                Sdk jdk = ProjectJdkTable.getInstance().findJdk(state.getFormattedJavaVersion());
                if (jdk != null) {
                    Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
                    if (projectSdk == null || !StringUtils.equals(Objects.requireNonNull(projectSdk).getName(), jdk.getName())) {
                        SdkConfigurationUtil.setDirectoryProjectSdk(project, jdk);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        state.setShowNotJenvJdkNotification(true);
        state.setProjectOpened(true);
    }

    public void changeJenvVersion(Project project, ProjectJenvState state) {
        Sdk jdk = ProjectJdkTable.getInstance().findJdk(state.getFormattedJavaVersion());
        SdkConfigurationUtil.setDirectoryProjectSdk(project, jdk);
        if (state.isChangeJdkByDialog() && state.isJenvJdkSelected()) {
            VirtualFile fileByNioPath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(state.getProjectJenvFilePath()));
            if (fileByNioPath != null && fileByNioPath.exists()) {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        fileByNioPath.setBinaryContent(state.getCurrentJavaVersion().getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                });
            }
            state.setChangeJdkByDialog(false);
        }
    }
}
