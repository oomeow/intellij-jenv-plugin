package com.github.jokingaboutlife.jenv.service;

import com.github.jokingaboutlife.jenv.JenvBundle;
import com.github.jokingaboutlife.jenv.config.JenvState;
import com.github.jokingaboutlife.jenv.constant.JdkExistsType;
import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.util.JenvNotifications;
import com.github.jokingaboutlife.jenv.util.JenvUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class JenvStateService {
    private final Project project;
    private final JenvState state;

    JenvStateService(Project project) {
        this.project = project;
        state = new JenvState();
    }

    public static JenvStateService getInstance(Project project) {
        return project.getService(JenvStateService.class);
    }

    public JenvState getState() {
        return state;
    }

    public void changeJenvJdkWithNotification(String jdkName) {
        if (StringUtils.isEmpty(jdkName)) {
            String title = JenvBundle.message("notification.jenv.file.empty.title");
            String content = JenvBundle.message("notification.jenv.file.empty.content");
            JenvNotifications.showWarnNotification(title, content, project, false);
            return;
        }
        JenvJdkTableService instance = JenvJdkTableService.getInstance();
        JenvJdkModel jenvJdkModel = instance.findJenvJdkByName(jdkName);
        if (jenvJdkModel == null) {
            String title = JenvBundle.message("notification.jdk.not.found.title");
            String content = JenvBundle.message("notification.jdk.not.found.content", jdkName);
            JenvNotifications.showErrorNotification(title, content, project, true);
            return;
        }
        JdkExistsType existsType = jenvJdkModel.getExistsType();
        if (JenvUtils.checkIsIdeaAndNotJenv(jenvJdkModel)) {
            String title = JenvBundle.message("notification.jdk.not.jenv.title");
            String content = JenvBundle.message("notification.jdk.not.jenv.content", jdkName);
            if (existsType.equals(JdkExistsType.OnlyMajorVersionMatch)) {
                if (state.isLocalJenvFileExists()) {
                    state.setNeedToChangeFile(true);
                }
                title = JenvBundle.message("notification.jdk.major.match.title");
                content = JenvBundle.message("notification.jdk.major.match.content");
            }
            JenvNotifications.showWarnNotification(title, content, project, true);
        } else if (existsType.equals(JdkExistsType.OnlyNameNotMatch)) {
            if (state.isLocalJenvFileExists()) {
                state.setNeedToChangeFile(true);
            }
            String title = JenvBundle.message("notification.jdk.name.not.match.title");
            String content = JenvBundle.message("notification.jdk.name.not.match.content", jdkName);
            JenvNotifications.showWarnNotification(title, content, project, true);
        }
        // do jdk change
        Sdk jdk = ProjectJdkTable.getInstance().findJdk(jdkName);
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (jdk != null && !jdk.equals(projectSdk)) {
            SdkConfigurationUtil.setDirectoryProjectSdk(project, jdk);
        }
    }

    public void changeJenvVersionFile(String handwrittenContent) {
        if (!state.isLocalJenvFileExists() || !state.isNeedToChangeFile()) {
            return;
        }
        Sdk changedJdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (changedJdk == null) {
            return;
        }
        String content;
        if (StringUtils.isNoneBlank(handwrittenContent)) {
            content = handwrittenContent;
        } else {
            String changedJdkName = changedJdk.getName();
            JenvJdkTableService instance = JenvJdkTableService.getInstance();
            JenvJdkModel jenvJdkModel = instance.findJenvJdkByName(changedJdkName);
            if (jenvJdkModel != null && JenvUtils.checkIsBoth(jenvJdkModel)) {
                content = changedJdkName;
            } else {
                content = null;
            }
        }
        String projectJenvFilePath = state.getLocalJenvFilePath();
        VirtualFile vProjectJenvFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(projectJenvFilePath));
        if (content != null && vProjectJenvFile != null && vProjectJenvFile.exists()) {
            ApplicationManager.getApplication().invokeAndWait(() -> ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    vProjectJenvFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    JenvNotifications.showErrorNotification("Change jEnv version File Failed", e.getMessage(), project, false);
                }
            }));
        }
    }

}
