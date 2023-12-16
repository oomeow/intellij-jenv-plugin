package com.github.jokingaboutlife.jenv.service;

import com.github.jokingaboutlife.jenv.config.JenvState;
import com.github.jokingaboutlife.jenv.constant.JenvConstants;
import com.github.jokingaboutlife.jenv.util.JenvNotifications;
import com.github.jokingaboutlife.jenv.util.JenvVersionParser;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

public class JenvService {
    private final String jEnvInstalledKey = "jEnv.installed";

    public static JenvService getInstance() {
        return ApplicationManager.getApplication().getService(JenvService.class);
    }

    public void setJenvInstalled(boolean jenvInstalled) {
        PropertiesComponent.getInstance().setValue(jEnvInstalledKey, jenvInstalled);
    }

    public boolean isJenvInstalled() {
        return PropertiesComponent.getInstance().getBoolean(jEnvInstalledKey, false);
    }

    public void initProject(Project project) {
        JenvState state = JenvStateService.getInstance(project).getState();
        String projectJdkVersionFilePath = project.getBasePath() + File.separator + JenvConstants.VERSION_FILE;
        VirtualFile projectJenvFile = VirtualFileManager.getInstance().findFileByNioPath(Path.of(projectJdkVersionFilePath));
        if (projectJenvFile != null && projectJenvFile.exists()) {
            state.setLocalJenvFileExists(true);
            state.setLocalJenvFilePath(projectJenvFile.getPath());
            try {
                String jdkName = new String(projectJenvFile.contentsToByteArray(), StandardCharsets.UTF_8).trim();
                Sdk jdk = ProjectJdkTable.getInstance().findJdk(jdkName);
                if (jdk != null) {
                    // find one JDK by name
                    Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
                    if (projectSdk == null || projectSdk != jdk) {
                        SdkConfigurationUtil.setDirectoryProjectSdk(project, jdk);
                    }
                } else {
                    // not found JDK, find again by JDK short version
                    Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
                    Arrays.sort(allJdks, (o1, o2) -> StringUtils.compare(o1.getName(), o2.getName()));
                    for (Sdk sdk : allJdks) {
                        if (sdk.getSdkType() instanceof JavaSdkType) {
                            String ideaShortVersion = JenvVersionParser.tryParseAndGetShortVersion(sdk.getVersionString());
                            if (ideaShortVersion != null && ideaShortVersion.equals(jdkName)) {
                                SdkConfigurationUtil.setDirectoryProjectSdk(project, sdk);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                JenvNotifications.showErrorNotification("Init project Failed", e.getMessage(), project, false);
            }
        }
    }

}
