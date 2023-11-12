package com.example.jenv.service;

import com.example.jenv.JenvBundle;
import com.example.jenv.JenvHelper;
import com.example.jenv.config.JenvState;
import com.example.jenv.constant.JenvConstants;
import com.example.jenv.constant.NotifyMessage;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.NotNull;

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

    public void changeJenvJdkWithNotification(String jdkVersion) {
//        System.out.println("Jenv First Version Changed........................");
        if (JenvHelper.checkIdeaJdkExistsByVersion(jdkVersion)) {
            if (!JenvHelper.checkIsJenvJdk(jdkVersion) && state.isShowNotJenvJdkNotification()) {
                // use resource bundle to format message
                String title = "Jenv warning";
                String subtitle = "Not Jenv JDK";
                String content = JenvBundle.message("notification.content.jdk.not.jenv", jdkVersion);
                NotifyMessage.NOT_JENV_JDK.setContent(content);

                Notification warnNotification = new Notification(JenvConstants.NOTIFICATION_GROUP_ID, title, content, NotificationType.WARNING);
                warnNotification.setSubtitle(subtitle);
                warnNotification.addAction(new NotificationAction("Don't show again") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                        if (e.getProject() != null) {
//                            JenvState projectJenvState = JenvStateService.getInstance(e.getProject()).getState();
                            state.setShowNotJenvJdkNotification(false);
                            notification.expire();
                        }
                    }
                });
                warnNotification.notify(project);
            }
            state.setCurrentJavaVersion(jdkVersion);
            Sdk jdk = ProjectJdkTable.getInstance().findJdk(jdkVersion);
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (jdk != null && jdk != projectSdk) {
                SdkConfigurationUtil.setDirectoryProjectSdk(project, jdk);
            }
        } else {
            String title = "JDK warning";
            String subtitle = "JDK not found";
            String content = JenvBundle.message("notification.content.jdk.not.found", jdkVersion);
            Notification errNotification = new Notification(JenvConstants.NOTIFICATION_GROUP_ID, title, content, NotificationType.ERROR);
            errNotification.setSubtitle(subtitle);
            errNotification.notify(project);
        }
    }
}
