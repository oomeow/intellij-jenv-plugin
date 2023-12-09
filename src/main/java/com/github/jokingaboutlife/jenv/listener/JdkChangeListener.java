package com.github.jokingaboutlife.jenv.listener;

import com.github.jokingaboutlife.jenv.service.JenvJdkTableService;
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
    }

    @Override
    public void jdkRemoved(@NotNull Sdk jdk) {
        @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            // if the deleted jdk is the current project jdk, set the project jdk to null before deleting it
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
    }

    @Override
    public void jdkNameChanged(@NotNull Sdk jdk, @NotNull String previousName) {
        JenvJdkTableService.getInstance().changeJenvJdkName(jdk, previousName);
    }
}
