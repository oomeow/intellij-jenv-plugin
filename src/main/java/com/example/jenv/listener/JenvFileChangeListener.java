package com.example.jenv.listener;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.JenvState;
import com.example.jenv.constant.DialogMessage;
import com.example.jenv.constant.JenvConstants;
import com.example.jenv.dialog.DefaultDialog;
import com.example.jenv.service.JenvService;
import com.example.jenv.service.JenvStateService;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
            Project currentProject = DataManager.getInstance().getDataContext().getData(PlatformDataKeys.PROJECT);
            String currentProjectJenvPath = currentProject.getBasePath() + File.separator + JenvConstants.VERSION_FILE.getName();
            if (!state.isChangeJenvByDialog()) {
                for (VFileEvent fileEvent : events) {
                    if (fileEvent.getPath().equals(currentProjectJenvPath)) {
                        try {
                            String jdkVersion = new String(fileEvent.getFile().contentsToByteArray()).trim();
                            if (JenvHelper.checkIdeaJDKExists(jdkVersion)) {
                                state.setProject(currentProject);
                                state.setCurrentJavaVersion(jdkVersion);
                                state.setChangeJenvByDialog(false);
                                JenvService service = ApplicationManager.getApplication().getService(JenvService.class);
                                service.changeJenvVersion();
//                            System.out.println(fileEvent.getFile().getName());
                            } else {
                                String description = DialogMessage.JDK_NOT_FOUND.getDescription();
                                String format = String.format(description, jdkVersion);
                                DialogMessage.JDK_NOT_FOUND.setDescription(format);
                                new DefaultDialog(DialogMessage.JDK_NOT_FOUND).show();
                            }
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            } else {
                state.setChangeJenvByDialog(false);
            }
        }
    }
}
