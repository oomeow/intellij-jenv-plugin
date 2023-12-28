package com.github.jokingaboutlife.jenv.listener;

import com.github.jokingaboutlife.jenv.config.JenvState;
import com.github.jokingaboutlife.jenv.constant.JenvConstants;
import com.github.jokingaboutlife.jenv.service.JenvStateService;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
                    currentProject = fileProject;
                } else if (requester instanceof PsiManager psiManager) {
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
                }
                currentProject = guessProject;
            }
            if (StringUtils.equals(currentProject.getBasePath(), jenvFile.getParent().getPath())) {
                JenvStateService stateService = JenvStateService.getInstance(currentProject);
                JenvState state = stateService.getState();
                if (fileEvent instanceof VFileCreateEvent) {
                    Document document = FileDocumentManager.getInstance().getDocument(jenvFile);
                    String jdkVersion = "";
                    if (document != null) {
                        jdkVersion = document.getText().trim();
                    }
                    if (StringUtils.isEmpty(jdkVersion)) {
                        // create by this plugin
                        state.setNeedToChangeFile(true);
                    } else {
                        // create by type `jenv local` command
                        state.setNeedToChangeFile(false);
                        stateService.changeJenvJdkWithNotification(jdkVersion);
                    }
                    state.setLocalJenvFileExists(true);
                    state.setLocalJenvFilePath(jenvFile.getPath());
                } else if (fileEvent instanceof VFileDeleteEvent) {
                    state.setLocalJenvFileExists(false);
                    state.setLocalJenvFilePath(null);
                } else if (fileEvent instanceof VFileContentChangeEvent changeEvent) {
                    // jenv version file content has changed
                    long jenvFileModificationStamp = state.getJenvFileModificationStamp();
                    long currentModificationStamp = changeEvent.getFile().getModificationStamp();
                    if (currentModificationStamp == jenvFileModificationStamp) {
                        // jenv version file has modified by other methods, and it means project JDK has changed, skip
                        continue;
                    }
                    try {
                        String jdkVersion = new String(jenvFile.contentsToByteArray()).trim();
                        stateService.changeJenvJdkWithNotification(jdkVersion);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
