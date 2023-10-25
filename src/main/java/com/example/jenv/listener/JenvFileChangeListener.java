package com.example.jenv.listener;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.JenvState;
import com.example.jenv.constant.JenvConstants;
import com.example.jenv.service.JenvService;
import com.example.jenv.service.JenvStateService;
import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class JenvFileChangeListener implements BulkFileListener {
    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
//        BulkFileListener.super.before(events);
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
//        BulkFileListener.super.after(events);
        JenvStateService jenvStateService = JenvStateService.getInstance();
        JenvState state = jenvStateService.getState();
        if (state.isProjectOpened()) {
            DataManager.getInstance().getDataContextFromFocusAsync().onSuccess(dataContext -> {
                Project currentProject = dataContext.getData(PlatformDataKeys.PROJECT);
                if (currentProject != null) {
                    String currentProjectJenvPath = currentProject.getBasePath() + File.separator + JenvConstants.VERSION_FILE;
                    if (!state.isChangeJenvByDialog()) {
                        for (VFileEvent fileEvent : events) {
                            if (fileEvent.getPath().equals(currentProjectJenvPath)) {
                                try {
                                    String jdkVersion = new String(Objects.requireNonNull(fileEvent.getFile()).contentsToByteArray()).trim();
                                    if (JenvHelper.checkIdeaJDKExists(jdkVersion)) {
                                        state.setProject(currentProject);
                                        state.setCurrentJavaVersion(jdkVersion);
                                        state.setChangeJenvByDialog(false);
                                        JenvService service = ApplicationManager.getApplication().getService(JenvService.class);
                                        service.changeJenvVersion();
//                            System.out.println(fileEvent.getFile().getName());
                                    } else {
                                        String title = "JDK not found";
                                        String content = "<html>Java version (%s) not found in Idea <br/> Please check JDK is exists and then open Project Structure and then add JDK.</html>";
                                        String format = String.format(content, jdkVersion);
                                        Notification notification = new Notification(JenvConstants.NOTIFICATION_GROUP_ID, title, format, NotificationType.ERROR);
                                        Notifications.Bus.notify(notification);
                                    }
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                    } else {
                        state.setChangeJenvByDialog(false);
                    }
                } else {
                    System.out.println("Not found project");
                }
            });
        }
    }
}
