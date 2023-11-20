package com.example.jenv.service;

import com.example.jenv.JenvBundle;
import com.example.jenv.config.JenvState;
import com.example.jenv.constant.JenvConstants;
import com.example.jenv.util.JenvNotifications;
import com.example.jenv.util.JenvUtils;
import com.example.jenv.util.JenvVersionParser;
import com.example.jenv.widget.JenvBarWidgetFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class JenvService {

    private boolean isJenvInstalled;

    public boolean isJenvInstalled() {
        return isJenvInstalled;
    }

    public static JenvService getInstance() {
        return ApplicationManager.getApplication().getService(JenvService.class);
    }

    public void initProject(Project project) {
        if (!isJenvInstalled) {
            if (JenvUtils.checkJenvInstalled()) {
                this.isJenvInstalled = true;
            } else {
                String title = JenvBundle.message("notification.jenv.not.installed.title");
                String content = JenvBundle.message("notification.jenv.not.installed.content");
                JenvNotifications.showErrorNotification(title, content, project, false);
                return;
            }
        }
        JenvState state = JenvStateService.getInstance(project).getState();
        String projectJdkVersionFilePath = project.getBasePath() + File.separator + JenvConstants.VERSION_FILE;
        VirtualFile projectJenvFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(projectJdkVersionFilePath));
        if (projectJenvFile != null && projectJenvFile.exists()) {
            state.setProjectJenvExists(true);
            state.setProjectJenvFilePath(projectJenvFile.getPath());
            try {
                String jdkVersion = new String(projectJenvFile.contentsToByteArray(), StandardCharsets.UTF_8).trim();
                Sdk jdk = ProjectJdkTable.getInstance().findJdk(jdkVersion);
                if (jdk != null) {
                    Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
                    if (projectSdk == null || projectSdk != jdk) {
                        SdkConfigurationUtil.setDirectoryProjectSdk(project, jdk);
                    }
                } else {
                    Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
                    for (Sdk sdk : allJdks) {
                        if (sdk.getSdkType() instanceof JavaSdkType) {
                            String ideaShortVersion = JenvVersionParser.tryParseAndGetShortVersion(sdk.getVersionString());
                            if (ideaShortVersion.equals(jdkVersion)) {
                                SdkConfigurationUtil.setDirectoryProjectSdk(project, sdk);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                JenvNotifications.showErrorNotification("Init project Failed", e.getMessage(), project, false);
            }
        }
        StatusBarWidgetsManager service = project.getService(StatusBarWidgetsManager.class);
        service.updateWidget(JenvBarWidgetFactory.class);
    }

}
