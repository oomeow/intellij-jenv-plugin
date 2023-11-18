package com.example.jenv.service;

import com.example.jenv.JenvBundle;
import com.example.jenv.config.JenvState;
import com.example.jenv.constant.JenvJdkExistsType;
import com.example.jenv.model.JenvJdkModel;
import com.example.jenv.util.JenvNotifications;
import com.example.jenv.util.JenvUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class JenvStateService {
    private final Project project;
    private final JenvState state;

    JenvStateService(Project project) {
        this.project = project;
        state = new JenvState();
    }

    public static JenvStateService getInstance(Project project) {
        return project.getService(JenvStateService.class);
    }

    public JenvState getState() {
        return state;
    }

    public void changeJenvJdkWithNotification(String jdkName) {
        JenvJdkTableService instance = JenvJdkTableService.getInstance();
        JenvJdkModel jenvJdkModel = instance.findJenvJdkByName(jdkName, true);
        if (jenvJdkModel != null) {
            JenvJdkExistsType existsType = jenvJdkModel.getExistsType();
            if (JenvUtils.checkIsIdeaAndNotJenv(jenvJdkModel)) {
                if (existsType.equals(JenvJdkExistsType.OnlyMajorVersionMatch)) {
                    String title = JenvBundle.message("notification.jdk.major.match.title");
                    String content = JenvBundle.message("notification.jdk.major.match.content");
                    JenvNotifications.showWarnNotification(title, content, project, true);
                    // todo: change .java-version file
                    changeJenvVersionFile();
                } else {
                    // use resource bundle to format message
                    String title = JenvBundle.message("notification.jdk.not.jenv.title");
                    String content = JenvBundle.message("notification.jdk.not.jenv.content", jdkName);
                    JenvNotifications.showWarnNotification(title, content, project, true);
                }
            } else {
                if (existsType.equals(JenvJdkExistsType.OnlyNameNotMatch)) {
                    String title = JenvBundle.message("notification.jdk.name.not.match.title");
                    String content = JenvBundle.message("notification.jdk.name.not.match.content", jdkName);
                    JenvNotifications.showWarnNotification(title, content, project, true);
                    // todo: show dialog to rename jdk name
                }
            }
            state.setCurrentJavaVersion(jdkName);
            Sdk jdk = ProjectJdkTable.getInstance().findJdk(jdkName);
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (jdk != null && jdk != projectSdk) {
                SdkConfigurationUtil.setDirectoryProjectSdk(project, jdk);
            }
        } else {
            String title = JenvBundle.message("notification.jdk.not.found.title");
            String content = JenvBundle.message("notification.jdk.not.found.content", jdkName);
            JenvNotifications.showErrorNotification(title, content, project, true);
        }
    }

    public void changeJenvVersionFile() {
        if (!state.isProjectJenvExists()) {
            return;
        }
        Sdk changedJdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (changedJdk == null) {
            return;
        }
        String changedJdkName = changedJdk.getName();
        JenvStateService stateService = JenvStateService.getInstance(project);
        JenvState projectState = stateService.getState();
        if (projectState.isFileHasChange()) {
            return;
        }
        JenvJdkTableService instance = JenvJdkTableService.getInstance();
        JenvJdkModel jenvJdkModel = instance.findJenvJdkByName(changedJdkName, true);
        if (JenvUtils.checkIsBoth(jenvJdkModel)) {
            projectState.setCurrentJavaVersion(changedJdkName);
            VirtualFile fileByNioPath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(projectState.getProjectJenvFilePath()));
            if (fileByNioPath != null && fileByNioPath.exists()) {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        fileByNioPath.setBinaryContent(changedJdkName.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                });
            }
        }
    }
}
