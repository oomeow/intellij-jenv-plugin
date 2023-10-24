package com.example.jenv.activity;

import com.example.jenv.service.JenvService;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class JenvProjectStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        JenvService service = ApplicationManager.getApplication().getService(JenvService.class);
        service.initProject(project);
    }
}
