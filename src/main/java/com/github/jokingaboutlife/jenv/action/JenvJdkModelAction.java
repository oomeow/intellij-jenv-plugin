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
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
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
            if (jenvJdkModel.getExistsType().equals(JdkExistsType.JEnvHomePathInvalid)) {
                showInvalidJdkMessage(project, projectSdk);
                return;
            }
            if (projectSdk != null && jenvJdkModel.getIdeaJdkInfo().getName().equals(projectSdk.getName())) {
                return;
            }
            String jdkName = jenvJdkModel.getName();
            JenvState state = JenvStateService.getInstance(project).getState();
            if (state.isLocalJenvFileExists()) {
                state.setNeedToChangeFile(true);
                JenvStateService.getInstance(project).changeJenvJdkWithNotification(jdkName);
            } else if (!jenvJdkModel.getExistsType().equals(JdkExistsType.OnlyInIDEA)) {
                // change jdk belong to jenv and the jenv version file not exists
                createJenvVersionFile(project, jdkName, state);
            }
        }
    }

    private void showInvalidJdkMessage(Project project, Sdk projectSdk) {
        String title = JenvBundle.message("messages.invalid.home.path.jdk.title");
        String content = JenvBundle.message("messages.invalid.home.path.jdk.content");
        int result = Messages.showYesNoDialog(project, content, title, AllIcons.General.Information);
        if (result == Messages.YES) {
            ApplicationManager.getApplication().invokeAndWait(() -> {
                if (projectSdk != null && jenvJdkModel.getIdeaJdkInfo().getName().equals(projectSdk.getName())) {
                    SdkConfigurationUtil.setDirectoryProjectSdk(project, null);
                }
                SdkConfigurationUtil.removeSdk(jenvJdkModel.getIdeaJdkInfo());
            });
        }
    }


    private static void createJenvVersionFile(Project project, String jdkName, JenvState state) {
        String title = JenvBundle.message("messages.create.jenv.version.file.title");
        String message = JenvBundle.message("messages.create.jenv.version.file.content");
        int result = Messages.showYesNoDialog(project, message, title, AllIcons.General.Information);
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
