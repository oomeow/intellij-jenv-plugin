package com.github.jokingaboutlife.jenv.activity;

import com.github.jokingaboutlife.jenv.constant.JdkExistsType;
import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.service.JenvJdkTableService;
import com.github.jokingaboutlife.jenv.service.JenvService;
import com.github.jokingaboutlife.jenv.service.JenvStateService;
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
                    if (jenvJdkModel.getExistsType().equals(JdkExistsType.OnlyMajorVersionMatch)
                            || jenvJdkModel.getExistsType().equals(JdkExistsType.OnlyNameNotMatch)) {
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
