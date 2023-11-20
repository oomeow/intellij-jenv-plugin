package com.example.jenv.listener;

import com.example.jenv.service.JenvJdkTableService;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;

public class JenvJdkChangeListener implements ProjectJdkTable.Listener {

    @Override
    public void jdkAdded(@NotNull Sdk jdk) {
        JenvJdkTableService.getInstance().addToJenvJdks(jdk);
    }

    @Override
    public void jdkRemoved(@NotNull Sdk jdk) {
        JenvJdkTableService.getInstance().removeFromJenvJdks(jdk);
    }

    @Override
    public void jdkNameChanged(@NotNull Sdk jdk, @NotNull String previousName) {
        JenvJdkTableService.getInstance().changeJenvJdkName(jdk, previousName);
    }
}
