package com.github.jokingaboutlife.jenv.widget;

import com.github.jokingaboutlife.jenv.JenvBundle;
import com.github.jokingaboutlife.jenv.constant.JdkExistsType;
import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.service.JenvJdkTableService;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Function;

public class JenvBanner implements EditorNotificationProvider {
    @Override
    public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
        return (Function<FileEditor, JComponent>) fileEditor -> {
            JenvJdkTableService.getInstance().validateJenvJdksFiles(project);
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (projectSdk != null) {
                JenvJdkModel jenvJdkModel = JenvJdkTableService.getInstance().findJenvJdkByName(projectSdk.getName());
                if (jenvJdkModel != null && jenvJdkModel.getExistsType().equals(JdkExistsType.JEnvHomePathInvalid)) {
                    EditorNotificationPanel panel = new EditorNotificationPanel(JBColor.red, EditorNotificationPanel.Status.Error);
                    String message = JenvBundle.message("notifications.banner.invalid.home.path.jdk.content", jenvJdkModel.getName(), jenvJdkModel.getHomePath());
                    panel.setText(message);
                    return panel;
                }
            }
            return null;
        };
    }
}
