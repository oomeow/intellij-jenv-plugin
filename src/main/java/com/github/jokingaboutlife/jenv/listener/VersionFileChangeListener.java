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
            if (currentProject == null) {
                Project guessProject = ProjectUtil.guessProjectForFile(jenvFile);
                if (guessProject == null) {
                    continue;
                } else {
                    currentProject = guessProject;
                }
            }
            if (StringUtils.equals(currentProject.getBasePath(), jenvFile.getParent().getPath())) {
                JenvStateService stateService = JenvStateService.getInstance(currentProject);
                JenvState state = stateService.getState();
                if (fileEvent instanceof VFileCreateEvent) {
                    try {
                        String jdkVersion = new String(jenvFile.contentsToByteArray()).trim();
                        if (StringUtils.isEmpty(jdkVersion)) {
                            // click jEnv status bar action and then show create jEnv version file dialog,
                            // the content of this file is empty, need to change file.
                            state.setNeedToChangeFile(true);
                        } else {
                            //  use jenv command `jenv local` to create file, the content of this file is not empty,
                            // no need to change file.
                            state.setNeedToChangeFile(false);
                            stateService.changeJenvJdkWithNotification(jdkVersion);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    state.setProjectJenvExists(true);
                    state.setProjectJenvFilePath(jenvFile.getPath());
                } else if (fileEvent instanceof VFileDeleteEvent) {
                    state.setProjectJenvExists(false);
                    state.setProjectJenvFilePath(null);
                } else if (fileEvent instanceof VFileContentChangeEvent changeEvent) {
                    // jenv version file content has changed
                    if (state.isNeedToChangeFile()) {
                        // if isNeedToChangeFile method return true means other tasks are modifying this file, so ignore this change event
                        continue;
                    }
                    try {
                        String jdkVersion = new String(changeEvent.getFile().contentsToByteArray()).trim();
                        stateService.changeJenvJdkWithNotification(jdkVersion);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
