package com.example.jenv.action;

import com.example.jenv.config.ProjectJenvState;
import com.example.jenv.constant.DialogMessage;
import com.example.jenv.dialog.DefaultDialog;
import com.example.jenv.dialog.JenvSelectDialog;
import com.example.jenv.service.JenvStateService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class JenvAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = Objects.requireNonNull(event.getProject());
        JenvStateService jenvStateService = project.getService(JenvStateService.class);
        ProjectJenvState state = jenvStateService.getState();
        if (state.isProjectOpened()) {
            if (!state.isJenvInstalled()) {
                new DefaultDialog(DialogMessage.JENV_NOT_INSTALL, project).show();
                return;
            }
            if (!state.isJavaInstalled()) {
                new DefaultDialog(DialogMessage.JAVA_NOT_INSTALL, project).show();
                return;
            }
            if (!state.isProjectJenvExists()) {
                new DefaultDialog(DialogMessage.PROJECT_JAVA_VERSION_NOT_FOUND, project).show();
                return;
            }
            JenvSelectDialog jenvSelectDialog = new JenvSelectDialog(project, DialogMessage.SELECT_JDK_VERSION.getTitle());
            jenvSelectDialog.pack();
            jenvSelectDialog.setLocationRelativeTo(null);
            jenvSelectDialog.setVisible(true);
        }
    }
}