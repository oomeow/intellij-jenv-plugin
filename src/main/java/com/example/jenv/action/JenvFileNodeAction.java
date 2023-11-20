package com.example.jenv.action;

import com.example.jenv.model.JenvJdkModel;
import com.example.jenv.util.JenvNotifications;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class JenvFileNodeAction extends DumbAwareAction {

    private final JenvJdkModel jenvJdkModel;

    public JenvFileNodeAction(JenvJdkModel jenvJdkModel) {
        super(jenvJdkModel.getName());
        this.jenvJdkModel = jenvJdkModel;
        Presentation presentation = getTemplatePresentation();
        presentation.setDescription(jenvJdkModel.getExistsType().getDescription());
        presentation.setIcon(AllIcons.General.OpenDisk);
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
            System.out.println(ex.getMessage());
        }
    }

}
