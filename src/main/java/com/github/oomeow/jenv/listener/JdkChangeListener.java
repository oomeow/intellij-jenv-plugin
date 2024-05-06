package com.github.oomeow.jenv.listener;

import com.github.oomeow.jenv.service.JenvJdkTableService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.NotNull;

public class JdkChangeListener implements ProjectJdkTable.Listener {

    @Override
    public void jdkAdded(@NotNull Sdk jdk) {
        JenvJdkTableService.getInstance().addToJenvJdks(jdk);
        ApplicationManager.getApplication().getMessageBus().syncPublisher(StatusBarUpdateMessage.TOPIC).updateStatusBar();
    }

    @Override
    public void jdkRemoved(@NotNull Sdk jdk) {
        @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            // if the deleted JDK is the current project JDK, set the project JDK to null before deleting it
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (projectSdk == null) {
                continue;
            }
            String projectJdkName = projectSdk.getName();
            String jdkName = jdk.getName();
            if (projectJdkName.equals(jdkName)) {
                ApplicationManager.getApplication().invokeLater(() -> SdkConfigurationUtil.setDirectoryProjectSdk(project, null));
            }
        }
        JenvJdkTableService.getInstance().removeFromJenvJdks(jdk);
        ApplicationManager.getApplication().getMessageBus().syncPublisher(StatusBarUpdateMessage.TOPIC).updateStatusBar();
    }

    @Override
    public void jdkNameChanged(@NotNull Sdk jdk, @NotNull String previousName) {
        JenvJdkTableService.getInstance().changeJenvJdkName(jdk, previousName);
        ApplicationManager.getApplication().getMessageBus().syncPublisher(StatusBarUpdateMessage.TOPIC).updateStatusBar();
    }
}
