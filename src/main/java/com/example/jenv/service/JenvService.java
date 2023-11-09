package com.example.jenv.service;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.ProjectJenvState;
import com.example.jenv.constant.JenvConstants;
import com.example.jenv.constant.NotifyMessage;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.projectRoots.impl.ProjectJdkTableImpl;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.remote.ui.SdkScopeController;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

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
        VirtualFile jenvFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(JenvConstants.JENV_DIR));
        JenvStateService jenvStateService = project.getService(JenvStateService.class);
        ProjectJenvState state = Objects.requireNonNull(jenvStateService.getState());

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

        // listen the project jdk change event
        ProjectRootManagerImpl.getInstanceImpl(project).addProjectJdkListener(() -> {
            Sdk changedJdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (changedJdk == null) {
                return;
            }
            String changedJdkVersion = changedJdk.getName();
            JenvStateService stateService = project.getService(JenvStateService.class);
            ProjectJenvState projectState = stateService.getState();
            if (projectState.isFileHasChange()) {
                return;
            }
            boolean isJenvJdk = JenvHelper.checkIsJenvJdk(changedJdkVersion);
            if (isJenvJdk) {
                projectState.setCurrentJavaVersion(changedJdkVersion);
                VirtualFile fileByNioPath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(projectState.getProjectJenvFilePath()));
                if (fileByNioPath != null && fileByNioPath.exists()) {
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        try {
                            fileByNioPath.setBinaryContent(changedJdkVersion.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    });
                }
            }
        });
    }

    public void changeJenvJdkWithNotification(Project currentProject, String jdkVersion, ProjectJenvState state) {
        if (JenvHelper.checkIdeaJdkExistsByVersion(jdkVersion)) {
            if (!JenvHelper.checkIsJenvJdk(jdkVersion) && state.isShowNotJenvJdkNotification()) {
                String formatMessage = String.format(NotifyMessage.NOT_JENV_JDK.getFormatTemplate(), jdkVersion);
                NotifyMessage.NOT_JENV_JDK.setContent(formatMessage);
                Notification warnNotification = JenvHelper.createWarnNotification(NotifyMessage.NOT_JENV_JDK);
                warnNotification.addAction(new NotificationAction("Don't show again") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                        if (e.getProject() != null) {
                            ProjectJenvState projectJenvState = e.getProject().getService(JenvStateService.class).getState();
                            projectJenvState.setShowNotJenvJdkNotification(false);
                            notification.expire();
                        }
                    }
                });
                warnNotification.notify(currentProject);
            }
            state.setCurrentJavaVersion(jdkVersion);
            Sdk jdk = ProjectJdkTable.getInstance().findJdk(state.getCurrentJavaVersion());
            Sdk projectSdk = ProjectRootManager.getInstance(currentProject).getProjectSdk();
            if (jdk != null && jdk != projectSdk) {
                SdkConfigurationUtil.setDirectoryProjectSdk(currentProject, jdk);
            }
        } else {
            String formatMessage = String.format(NotifyMessage.NOT_FOUND_JDK.getFormatTemplate(), jdkVersion);
            NotifyMessage.NOT_FOUND_JDK.setContent(formatMessage);
            Notification errorNotification = JenvHelper.createErrorNotification(NotifyMessage.NOT_FOUND_JDK);
            errorNotification.notify(currentProject);
        }
    }

}
