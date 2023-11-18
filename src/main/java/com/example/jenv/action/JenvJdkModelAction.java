package com.example.jenv.action;

import com.example.jenv.config.JenvState;
import com.example.jenv.model.JenvJdkModel;
import com.example.jenv.service.JenvStateService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class JenvJdkModelAction extends DumbAwareAction {

    private final JenvJdkModel jenvJdkModel;

    public JenvJdkModelAction(JenvJdkModel jenvJdkModel, boolean isCurrentJdk) {
        super(jenvJdkModel.getName());
        this.jenvJdkModel = jenvJdkModel;
        Presentation presentation = getTemplatePresentation();
        presentation.setDescription(jenvJdkModel.getExistsType().getDescription());
        if (isCurrentJdk) {
            presentation.setIcon(AllIcons.General.InspectionsOK);
        } else {
            presentation.setIcon(jenvJdkModel.getExistsType().getIcon());
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            JenvState state = JenvStateService.getInstance(project).getState();
            state.setFileHasChange(false);
            state.setCurrentJavaVersion(jenvJdkModel.getVersion());
            JenvStateService.getInstance(project).changeJenvJdkWithNotification(jenvJdkModel.getName());
        }
    }
}
