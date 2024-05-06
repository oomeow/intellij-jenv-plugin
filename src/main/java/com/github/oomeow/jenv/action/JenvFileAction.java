package com.github.oomeow.jenv.action;

import com.github.oomeow.jenv.model.JenvJdkModel;
import com.github.oomeow.jenv.util.JenvNotifications;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class JenvFileAction extends DumbAwareAction {

    private final JenvJdkModel jenvJdkModel;

    public JenvFileAction(JenvJdkModel jenvJdkModel) {
        super(jenvJdkModel.getName());
        this.jenvJdkModel = jenvJdkModel;
        Presentation presentation = getTemplatePresentation();
        presentation.setDescription(jenvJdkModel.getExistsType().getDescription());
        presentation.setIcon(AllIcons.Actions.MenuOpen);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            Project project = e.getProject();
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(new File(jenvJdkModel.getHomePath()));
            } else {
                JenvNotifications.showWarnNotification("Open folder failed", "Desktop OPEN action not supported", project, false);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
