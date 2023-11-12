package com.example.jenv.action;

import com.example.jenv.config.JenvState;
import com.example.jenv.model.JenvSdkModel;
import com.example.jenv.service.JenvJdkTableService;
import com.example.jenv.service.JenvStateService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class JenvJdkModelAction extends DumbAwareAction {

    private JenvSdkModel jenvSdkModel;

    public JenvJdkModelAction(JenvSdkModel jenvSdkModel) {
        super(jenvSdkModel.getName());
        this.jenvSdkModel = jenvSdkModel;
        Presentation presentation = getTemplatePresentation();
        if (JenvJdkTableService.getInstance().checkIsIdea(jenvSdkModel)) {
            presentation.setDescription(jenvSdkModel.getExistsType().getDescription());
            presentation.setIcon(jenvSdkModel.getExistsType().getIcon());
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        System.out.println("--------------------------------------------");
        System.out.println(jenvSdkModel.getHomePath());
        if (JenvJdkTableService.getInstance().checkIsIdea(jenvSdkModel)) {
            System.out.println("jenvSdkModel.getIdeaJdkInfo() = " + jenvSdkModel.getIdeaJdkInfo());
            Project project = e.getProject();
            if (project != null) {
                JenvState state = JenvStateService.getInstance(project).getState();
                state.setFileHasChange(false);
                state.setCurrentJavaVersion(jenvSdkModel.getVersion());
                JenvStateService.getInstance(project).changeJenvJdkWithNotification(jenvSdkModel.getName());
            }
        } else {
            System.out.println(jenvSdkModel.getCanonicalPath());
        }
        System.out.println("--------------------------------------------");
    }
}
