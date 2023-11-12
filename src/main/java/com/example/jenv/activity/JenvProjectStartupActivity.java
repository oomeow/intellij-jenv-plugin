package com.example.jenv.activity;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.JenvState;
import com.example.jenv.service.JenvJdkTableService;
import com.example.jenv.service.JenvService;
import com.example.jenv.service.JenvStateService;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class JenvProjectStartupActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        if (ApplicationManager.getApplication().isUnitTestMode() || project.isDisposed()) {
            return;
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            ApplicationManager.getApplication().invokeLater(() -> {
                JenvService.getInstance().initProject(project);
                JenvJdkTableService.getInstance().refreshJenvJdks();

                // listen the project jdk change event
                ProjectRootManagerImpl.getInstanceImpl(project).addProjectJdkListener(() -> {
                    Sdk changedJdk = ProjectRootManager.getInstance(project).getProjectSdk();
                    if (changedJdk == null) {
                        return;
                    }
                    String changedJdkVersion = changedJdk.getName();
                    JenvStateService stateService = JenvStateService.getInstance(project);
                    JenvState projectState = stateService.getState();
                    if (projectState.isFileHasChange()) {
                        return;
                    }
                    boolean isJenvJdk = JenvHelper.checkIsJenvJdk(changedJdkVersion);
                    if (isJenvJdk) {
                        projectState.setCurrentJavaVersion(changedJdkVersion);
                        VirtualFile fileByNioPath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(projectState.getProjectJenvFilePath()));
                        if (fileByNioPath != null && fileByNioPath.exists()) {
                            ApplicationManager.getApplication().runWriteAction(() -> {
                                try {
                                    fileByNioPath.setBinaryContent(changedJdkVersion.getBytes(StandardCharsets.UTF_8));
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            });
                        }
                    }
                });
            });
        });

    }
}
