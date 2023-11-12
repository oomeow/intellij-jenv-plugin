package com.example.jenv.service;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.JenvState;
import com.example.jenv.constant.JenvConstants;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class JenvService {

    public static JenvService getInstance() {
        //
        PropertiesComponent.getInstance();
        return ApplicationManager.getApplication().getService(JenvService.class);
    }

    public void initProject(Project project) {
        VirtualFile jenvFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(JenvConstants.JENV_DIR));
        JenvStateService jenvStateService = JenvStateService.getInstance(project);
        JenvState state = Objects.requireNonNull(jenvStateService.getState());

        if (jenvFile != null && jenvFile.exists()) {
            state.setJenvInstalled(true);
        }
        if (CollectionUtils.isNotEmpty(JenvHelper.getAllIdeaJdkVersionList())) {
            state.setJavaInstalled(true);
        }
        JenvHelper.refreshAllJenvJdkInfo();

        String projectJdkVersionFilePath = project.getBasePath() + File.separator + JenvConstants.VERSION_FILE;
        VirtualFile projectJenvFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(projectJdkVersionFilePath));
        if (projectJenvFile != null && projectJenvFile.exists()) {
            state.setProjectJenvExists(true);
            state.setProjectJenvFilePath(projectJenvFile.getPath());
            try {
                Path path = Paths.get(projectJenvFile.getPath());
                String jdkVersion = Files.readString(path).trim();
                state.setCurrentJavaVersion(jdkVersion);
                Sdk jdk = ProjectJdkTable.getInstance().findJdk(state.getCurrentJavaVersion());
                if (jdk != null) {
                    Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
                    if (projectSdk == null || projectSdk != jdk) {
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


}
