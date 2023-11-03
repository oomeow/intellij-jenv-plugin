package com.example.jenv.action;

import com.example.jenv.config.JenvState;
import com.example.jenv.constant.DialogMessage;
import com.example.jenv.dialog.DefaultDialog;
import com.example.jenv.dialog.JenvDialog;
import com.example.jenv.service.JenvStateService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class JenvAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        JenvState state = JenvStateService.getInstance().getState();
        if (state.isProjectOpened()) {
            state.setProject(event.getProject());
            if (!state.isJenvInstalled()) {
                new DefaultDialog(DialogMessage.JENV_NOT_INSTALL).show();
                return;
            }
            if (!state.isJavaInstalled()) {
                new DefaultDialog(DialogMessage.JAVA_NOT_INSTALL).show();
                return;
            }
            if (!state.isProjectJenvExists()) {
                new DefaultDialog(DialogMessage.PROJECT_JAVA_VERSION_NOT_FOUND).show();
                return;
            }
            new JenvDialog(DialogMessage.SELECT_JDK_VERSION).show();
        }
    }
}