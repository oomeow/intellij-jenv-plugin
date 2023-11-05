package com.example.jenv.listener;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.ProjectJenvState;
import com.example.jenv.constant.JenvConstants;
import com.example.jenv.service.JenvService;
import com.example.jenv.service.JenvStateService;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class JenvFileChangeListener implements BulkFileListener {

    boolean isJenvFileChange = false;
    Project currentProject = null;
    VFileEvent jenvFileEvent = null;

    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        Project guessProject = null;
        for (VFileEvent fileEvent : events) {
            if (fileEvent.getPath().endsWith(JenvConstants.VERSION_FILE)) {
                @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                VirtualFile jenvFile = Objects.requireNonNull(fileEvent.getFile());
                for (Project openProject : openProjects) {
                    boolean inContent = ProjectRootManager.getInstance(openProject).getFileIndex().isInContent(jenvFile);
                    if (inContent) {
                        guessProject = openProject;
                        break;
                    }
                }
                if (guessProject == null) {
                    continue;
                }
                ProjectJenvState state = guessProject.getService(JenvStateService.class).getState();
                if (!isJenvFileChange && StringUtils.equals(guessProject.getBasePath(), jenvFile.getParent().getPath()) && !state.isChangeJdkByDialog()) {
                    isJenvFileChange = true;
                    currentProject = guessProject;
                    jenvFileEvent = fileEvent;
                }
                break;
            }
        }
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        if (isJenvFileChange && currentProject != null) {
            ProjectJenvState state = currentProject.getService(JenvStateService.class).getState();
            try {
                String jdkVersion = new String(Objects.requireNonNull(jenvFileEvent.getFile()).contentsToByteArray()).trim();
                if (JenvHelper.checkIdeaJDKExists(jdkVersion)) {
                    if (!JenvHelper.checkIsJenvJdk(jdkVersion) && state.isShowNotJenvJdkNotification()) {
                        String title = "JDK not belong to Jenv";
                        String content = "<html>Java version (%s) found in Idea, but not belong to Jenv <br/> The JDK of project will be change, event it not belong to Jenv. if you want to use JDK of Jenv, you need to change the path of the JDK.</html>";
                        String format = String.format(content, jdkVersion);
                        Notification notification = new Notification(JenvConstants.NOTIFICATION_GROUP_ID, title, format, NotificationType.WARNING);
                        notification.addAction(new NotificationAction("Don't show again") {
                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                ProjectJenvState projectJenvState = Objects.requireNonNull(e.getProject()).getService(JenvStateService.class).getState();
                                projectJenvState.setShowNotJenvJdkNotification(false);
                                notification.expire();
                            }
                        });
                        notification.notify(currentProject);
                    }
                    state.setCurrentJavaVersion(jdkVersion);
                    state.setChangeJdkByDialog(false);
                    JenvService service = JenvService.getInstance();
                    service.changeJenvVersion(currentProject, state);
                } else {
                    String title = "JDK not found";
                    String content = "<html>Java version (%s) not found in Idea. <br/> Please check JDK is exists and then open Project Structure to add JDK.</html>";
                    String format = String.format(content, jdkVersion);
                    Notification notification = new Notification(JenvConstants.NOTIFICATION_GROUP_ID, title, format, NotificationType.ERROR);
                    notification.notify(currentProject);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                isJenvFileChange = false;
                currentProject = null;
                jenvFileEvent = null;
            }
        }
    }
}
