package com.github.oomeow.jenv.widget;

import com.github.oomeow.jenv.service.JenvService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class JenvBarWidgetFactory implements StatusBarWidgetFactory {
    @Override
    public @NotNull @NonNls String getId() {
        return JenvBarWidget.JENV_STATUS_BAR_ID;
    }

    @Override
    public @NotNull String getDisplayName() {
        return JenvBarWidget.JENV_STATUS_BAR_DISPLAY_NAME;
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return JenvService.getInstance().isJenvInstalled();
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new JenvBarWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
