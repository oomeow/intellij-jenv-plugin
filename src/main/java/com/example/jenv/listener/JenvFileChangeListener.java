package com.example.jenv.listener;

import com.example.jenv.config.JenvState;
import com.example.jenv.constant.JenvConstants;
import com.example.jenv.service.JenvStateService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class JenvFileChangeListener implements BulkFileListener {


    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        // change before
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        boolean isJenvFileChange = false;
        Project currentProject = null;
        VFileContentChangeEvent fileContentChangeEvent = null;
        for (VFileEvent fileEvent : events) {
            if (fileEvent instanceof VFileContentChangeEvent changeEvent) {
                if (!changeEvent.getPath().endsWith(JenvConstants.VERSION_FILE)) {
                    continue;
                }
                // get the project which include the jenv version file
                VirtualFile jenvFile = Objects.requireNonNull(changeEvent.getFile());
                Project guessProject = ProjectLocator.getInstance().guessProjectForFile(jenvFile);
                if (guessProject == null) {
                    continue;
                }
                if (StringUtils.equals(guessProject.getBasePath(), jenvFile.getParent().getPath())) {
                    JenvState guessState = JenvStateService.getInstance(guessProject).getState();
                    if (!guessState.isFileHasChange()) {
                        isJenvFileChange = true;
                        currentProject = guessProject;
                        fileContentChangeEvent = changeEvent;
                        guessState.setFileHasChange(true);
                        break;
                    }
                }
            }
        }
        if (isJenvFileChange) {
            JenvState state = JenvStateService.getInstance(currentProject).getState();
            String jdkVersion = null;
            try {
                jdkVersion = new String(Objects.requireNonNull(fileContentChangeEvent.getFile()).contentsToByteArray()).trim();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            JenvStateService.getInstance(currentProject).changeJenvJdkWithNotification(jdkVersion);
            state.setFileHasChange(false);
        }
    }
}
