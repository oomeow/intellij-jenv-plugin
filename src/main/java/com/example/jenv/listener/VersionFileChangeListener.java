package com.example.jenv.listener;

import com.example.jenv.config.JenvState;
import com.example.jenv.constant.JenvConstants;
import com.example.jenv.service.JenvStateService;
import com.example.jenv.util.JenvNotifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class VersionFileChangeListener implements BulkFileListener {

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
                VirtualFile jenvFile = Objects.requireNonNull(changeEvent.getFile());
                Project guessProject = ProjectUtil.guessProjectForFile(jenvFile);
                if (guessProject == null) {
                    continue;
                }
                if (StringUtils.equals(guessProject.getBasePath(), jenvFile.getParent().getPath())) {
                    JenvState guessState = JenvStateService.getInstance(guessProject).getState();
                    if (!guessState.isFileChanged() && !guessState.isNeedToChangeFile()) {
                        isJenvFileChange = true;
                        currentProject = guessProject;
                        fileContentChangeEvent = changeEvent;
                        guessState.setFileChanged(true);
                        break;
                    }
                }
            }
        }
        if (isJenvFileChange) {
            try {
                String jdkVersion = new String(fileContentChangeEvent.getFile().contentsToByteArray()).trim();
                JenvStateService.getInstance(currentProject).changeJenvJdkWithNotification(jdkVersion);
            } catch (IOException e) {
                JenvNotifications.showErrorNotification("Jenv File Changed, but read content failed", e.getMessage(), currentProject, false);
            } finally {
                JenvStateService.getInstance(currentProject).getState().setFileChanged(false);
            }
        }
    }
}
