package com.example.jenv.listener;

import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;

public class JenvJdkAddListener implements ProjectJdkTable.Listener {
    @Override
    public void jdkAdded(@NotNull Sdk jdk) {
        System.out.println("Add Jdk ............" + jdk.getName());
    }

    @Override
    public void jdkRemoved(@NotNull Sdk jdk) {
        System.out.println("Removed Jdk ............" + jdk.getName());
    }

    @Override
    public void jdkNameChanged(@NotNull Sdk jdk, @NotNull String previousName) {
        System.out.println("Rename Jdk ..............." + previousName);
    }
}
