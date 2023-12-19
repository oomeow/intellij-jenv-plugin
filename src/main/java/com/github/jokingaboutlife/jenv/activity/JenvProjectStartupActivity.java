package com.github.jokingaboutlife.jenv.activity;

import com.github.jokingaboutlife.jenv.JenvBundle;
import com.github.jokingaboutlife.jenv.constant.JdkExistsType;
import com.github.jokingaboutlife.jenv.listener.StatusBarUpdateMessage;
import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.service.JenvJdkTableService;
import com.github.jokingaboutlife.jenv.service.JenvService;
import com.github.jokingaboutlife.jenv.service.JenvStateService;
import com.github.jokingaboutlife.jenv.util.JenvNotifications;
import com.github.jokingaboutlife.jenv.util.JenvUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;

public class JenvProjectStartupActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        if (ApplicationManager.getApplication().isUnitTestMode() || project.isDisposed()) {
            return;
        }
        if (!JenvService.getInstance().isJenvInstalled()) {
            if (JenvUtils.checkJenvInstalled()) {
                JenvService.getInstance().setJenvInstalled(true);
            } else {
                ApplicationManager.getApplication().invokeLater(() -> {
                    String title = JenvBundle.message("notification.jenv.not.installed.title");
                    String content = JenvBundle.message("notification.jenv.not.installed.content");
                    JenvNotifications.showErrorNotification(title, content, project, false);
                });
                return;
            }
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> ApplicationManager.getApplication().invokeLater(() -> {
            // init
            JenvJdkTableService.getInstance().refreshJenvJdks();
            JenvService.getInstance().initProject(project);
            ApplicationManager.getApplication().getMessageBus().syncPublisher(StatusBarUpdateMessage.TOPIC).updateStatusBar();
            // project JDK changed event
            ProjectRootManagerImpl.getInstanceImpl(project).addProjectJdkListener(() -> {
                // update status bar
                ApplicationManager.getApplication().getMessageBus().syncPublisher(StatusBarUpdateMessage.TOPIC).updateStatusBar();
                // update jEnv banner
                EditorNotifications.getInstance(project).updateAllNotifications();
                Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
                if (projectSdk != null) {
                    JenvStateService stateService = JenvStateService.getInstance(project);
                    JenvJdkModel jenvJdkModel = JenvJdkTableService.getInstance().findJenvJdkByName(projectSdk.getName());
                    if (jenvJdkModel.getExistsType().equals(JdkExistsType.OnlyMajorVersionMatch)
                            || jenvJdkModel.getExistsType().equals(JdkExistsType.OnlyNameNotMatch)) {
                        stateService.changeJenvVersionFile(jenvJdkModel.getShortVersion());
                    } else {
                        stateService.changeJenvVersionFile(null);
                    }
                    stateService.getState().setNeedToChangeFile(false);
                }
            });
        }));
    }

}
