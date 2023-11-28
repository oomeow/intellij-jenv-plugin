package com.github.jokingaboutlife.jenv.action;

import com.github.jokingaboutlife.jenv.JenvBundle;
import com.github.jokingaboutlife.jenv.config.JenvState;
import com.github.jokingaboutlife.jenv.constant.JdkExistsType;
import com.github.jokingaboutlife.jenv.constant.JenvConstants;
import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.service.JenvStateService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Paths;

public class JenvJdkModelAction extends DumbAwareAction {

    private final JenvJdkModel jenvJdkModel;

    public JenvJdkModelAction(JenvJdkModel jenvJdkModel, boolean isCurrentJdk) {
        super(jenvJdkModel.getName());
        this.jenvJdkModel = jenvJdkModel;
        Presentation presentation = getTemplatePresentation();
        presentation.setDescription(jenvJdkModel.getExistsType().getDescription());
        if (isCurrentJdk) {
            presentation.setIcon(AllIcons.Actions.SetDefault);
        } else {
            presentation.setIcon(jenvJdkModel.getExistsType().getIcon());
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        if (project != null) {
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (projectSdk != null && jenvJdkModel.getIdeaJdkInfo().getName().equals(projectSdk.getName())) {
                return;
            }
            String jdkName = jenvJdkModel.getName();
            JenvState state = JenvStateService.getInstance(project).getState();
            if (state.isProjectJenvExists()) {
                state.setNeedToChangeFile(true);
                JenvStateService.getInstance(project).changeJenvJdkWithNotification(jdkName);
                return;
            }
            if (!jenvJdkModel.getExistsType().equals(JdkExistsType.OnlyInIDEA)) {
                String title = JenvBundle.message("messages.create.jenv.version.file.title");
                String message = JenvBundle.message("messages.create.jenv.version.file.content");
                int result = Messages.showYesNoDialog(message, title, AllIcons.General.Information);
                if (result == Messages.YES) {
                    ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
                        try {
                            String basePath = project.getBasePath();
                            if (basePath != null) {
                                VirtualFile baseDir = VirtualFileManager.getInstance().findFileByNioPath(Paths.get(basePath));
                                if (baseDir != null) {
                                    VirtualFile jenvFile = baseDir.createChildData(project, JenvConstants.VERSION_FILE);
                                    JenvStateService.getInstance(project).changeJenvJdkWithNotification(jdkName);
                                    VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
                                    PsiFile psiFile = PsiManager.getInstance(project).findFile(jenvFile);
                                    if (psiFile != null) {
                                        psiFile.navigate(true);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
                } else {
                    state.setNeedToChangeFile(false);
                    JenvStateService.getInstance(project).changeJenvJdkWithNotification(jdkName);
                }
            }
        }
    }

}
