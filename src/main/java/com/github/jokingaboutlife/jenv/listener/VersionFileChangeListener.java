package com.github.jokingaboutlife.jenv.listener;

import com.github.jokingaboutlife.jenv.config.JenvState;
import com.github.jokingaboutlife.jenv.constant.JenvConstants;
import com.github.jokingaboutlife.jenv.service.JenvStateService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class VersionFileChangeListener implements BulkFileListener {

    @Override
    public void before(@NotNull List<? extends @NotNull VFileEvent> events) {
        // change before
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        Project currentProject = null;
        for (VFileEvent fileEvent : events) {
            if (!fileEvent.getPath().endsWith(JenvConstants.VERSION_FILE)) {
                continue;
            }
            // create or delete event probably get current project by the following way
            if (fileEvent instanceof VFileCreateEvent || fileEvent instanceof VFileDeleteEvent) {
                Object requester = fileEvent.getRequestor();
                if (requester instanceof Project fileProject) {
                    // create event
                    currentProject = fileProject;
                } else if (requester instanceof PsiManager psiManager) {
                    // delete event
                    currentProject = psiManager.getProject();
                }
            }
            VirtualFile jenvFile = fileEvent.getFile();
            if (jenvFile == null) {
                continue;
            }
            // currentProject still null, use guessProject method to find this current project
            if (currentProject == null) {
                Project guessProject = ProjectUtil.guessProjectForFile(jenvFile);
                if (guessProject == null) {
                    continue;
                } else {
                    currentProject = guessProject;
                }
            }
            if (StringUtils.equals(currentProject.getBasePath(), jenvFile.getParent().getPath())) {
                // jenv version file has deleted or created
                JenvStateService stateService = JenvStateService.getInstance(currentProject);
                JenvState state = stateService.getState();
                if (fileEvent instanceof VFileCreateEvent || fileEvent instanceof VFileDeleteEvent) {
                    if (fileEvent instanceof VFileCreateEvent) {
                        try {
                            state.setFileChanged(true);
                            String jdkVersion = new String(jenvFile.contentsToByteArray()).trim();
                            // create Jenv version file:
                            //  1. by jenv command, its value is not empty.
                            //  2. by this plugin dialog, its value is empty.
                            if (!StringUtils.isEmpty(jdkVersion)) {
                                stateService.changeJenvJdkWithNotification(jdkVersion);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } finally {
                            state.setFileChanged(false);
                        }
                        state.setProjectJenvExists(true);
                        state.setProjectJenvFilePath(jenvFile.getPath());
                    } else {
                        state.setProjectJenvExists(false);
                        state.setProjectJenvFilePath(null);
                    }
                }
                // jenv version file content has changed
                if (fileEvent instanceof VFileContentChangeEvent changeEvent) {
                    if (!state.isFileChanged() && !state.isNeedToChangeFile()) {
                        state.setFileChanged(true);
                        try {
                            String jdkVersion = new String(changeEvent.getFile().contentsToByteArray()).trim();
                            stateService.changeJenvJdkWithNotification(jdkVersion);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } finally {
                            state.setFileChanged(false);
                        }
                    }
                }
            }
        }
    }
}
