package com.example.jenv.listener;

import com.example.jenv.config.ProjectJenvState;
import com.example.jenv.constant.JenvConstants;
import com.example.jenv.service.JenvService;
import com.example.jenv.service.JenvStateService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
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
        VFileEvent jenvFileEvent = null;
        for (VFileEvent fileEvent : events) {
            if (fileEvent.getPath().endsWith(JenvConstants.VERSION_FILE)) {
                // get the project which include the jenv version file
                @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
                VirtualFile jenvFile = Objects.requireNonNull(fileEvent.getFile());
                for (Project openProject : openProjects) {
                    boolean inContent = ProjectRootManager.getInstance(openProject).getFileIndex().isInContent(jenvFile);
                    if (inContent) {
                        if (StringUtils.equals(openProject.getBasePath(), jenvFile.getParent().getPath())) {
                            ProjectJenvState openState = openProject.getService(JenvStateService.class).getState();
                            if (!openState.isFileHasChange()) {
                                isJenvFileChange = true;
                                currentProject = openProject;
                                jenvFileEvent = fileEvent;
                                openState.setFileHasChange(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (isJenvFileChange) {
            ProjectJenvState state = currentProject.getService(JenvStateService.class).getState();
            String jdkVersion = null;
            try {
                jdkVersion = new String(Objects.requireNonNull(jenvFileEvent.getFile()).contentsToByteArray()).trim();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            JenvService.getInstance().changeJenvJdkWithNotification(currentProject, jdkVersion, state);
            state.setFileHasChange(false);
        }

    }
}
