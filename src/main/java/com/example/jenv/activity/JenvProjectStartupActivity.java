package com.example.jenv.activity;

import com.example.jenv.service.JenvService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class JenvProjectStartupActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            JenvService service = JenvService.getInstance();
            service.initProject(project);
        });
    }
}
