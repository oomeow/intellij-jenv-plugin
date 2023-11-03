package com.example.jenv.listener;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.JenvState;
import com.example.jenv.constant.JenvConstants;
import com.example.jenv.service.JenvService;
import com.example.jenv.service.JenvStateService;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class JenvFileChangeListener implements BulkFileListener {

    boolean isJenvFileChange = false;
    Project currentProject = null;
    VFileEvent jenvFileEvent = null;

    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        for (VFileEvent fileEvent : events) {
            if (fileEvent.getPath().endsWith(JenvConstants.VERSION_FILE)) {
                JenvState state = JenvStateService.getInstance().getState();
                if (state.isProjectOpened()) {
                    VirtualFile file = Objects.requireNonNull(fileEvent.getFile());
                    Project guessProject = ProjectLocator.getInstance().guessProjectForFile(file);
                    if (guessProject == null) {
                        continue;
                    }
                    if (Objects.equals(guessProject.getBasePath(), file.getParent().getPath()) && !state.isChangeJenvByDialog()) {
                        isJenvFileChange = true;
                        currentProject = ProjectLocator.getInstance().guessProjectForFile(file);
                        jenvFileEvent = fileEvent;
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        JenvState state = JenvStateService.getInstance().getState();
        if (isJenvFileChange) {
            if (currentProject != null) {
                try {
                    String jdkVersion = new String(Objects.requireNonNull(jenvFileEvent.getFile()).contentsToByteArray()).trim();
                    if (JenvHelper.checkIdeaJDKExists(jdkVersion)) {
                        state.setProject(currentProject);
                        state.setCurrentJavaVersion(jdkVersion);
                        state.setChangeJenvByDialog(false);
                        JenvService service = ApplicationManager.getApplication().getService(JenvService.class);
                        service.changeJenvVersion();
                    } else {
                        String title = "JDK not found";
                        String content = "<html>Java version (%s) not found in Idea <br/> Please check JDK is exists and then open Project Structure and then add JDK.</html>";
                        String format = String.format(content, jdkVersion);
                        Notification notification = new Notification(JenvConstants.NOTIFICATION_GROUP_ID, title, format, NotificationType.ERROR);
                        Notifications.Bus.notify(notification);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    isJenvFileChange = false;
                    currentProject = null;
                    jenvFileEvent = null;
                }
            } else {
                System.out.println("Not found project");
            }
        }
    }
}
