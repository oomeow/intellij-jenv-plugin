package com.example.jenv.activity;

import com.example.jenv.constant.JenvJdkExistsType;
import com.example.jenv.model.JenvJdkModel;
import com.example.jenv.service.JenvJdkTableService;
import com.example.jenv.service.JenvService;
import com.example.jenv.service.JenvStateService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class JenvProjectStartupActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        if (ApplicationManager.getApplication().isUnitTestMode() || project.isDisposed()) {
            return;
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> ApplicationManager.getApplication().invokeLater(() -> {
            JenvService.getInstance().initProject(project);
            JenvJdkTableService.getInstance().refreshJenvJdks();
            ProjectRootManagerImpl.getInstanceImpl(project).addProjectJdkListener(() -> {
                Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
                if (projectSdk != null) {
                    JenvJdkModel jenvJdkModel = JenvJdkTableService.getInstance().findJenvJdkByName(projectSdk.getName());
                    if (jenvJdkModel.getExistsType().equals(JenvJdkExistsType.OnlyMajorVersionMatch)
                            || jenvJdkModel.getExistsType().equals(JenvJdkExistsType.OnlyNameNotMatch)) {
                        JenvStateService.getInstance(project).changeJenvVersionFile(jenvJdkModel.getShortVersion());
                    } else {
                        JenvStateService.getInstance(project).changeJenvVersionFile(null);
                    }
                    JenvStateService.getInstance(project).getState().setNeedToChangeFile(false);
                }
            });
        }));
    }

}
