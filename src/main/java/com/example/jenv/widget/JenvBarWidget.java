package com.example.jenv.widget;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.ui.ClickListener;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class JenvBarWidget extends TextPanel implements CustomStatusBarWidget {

    private final Project project;
//    private boolean isLoadingJenv;
    public static final String JENV_STATUS_BAR_ID = "Jenv.Widget";

    JenvBarWidget(Project project) {
        this.project = project;
        setTextAlignment(CENTER_ALIGNMENT);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public @NotNull @NonNls String ID() {
        return JENV_STATUS_BAR_ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        if (project.isDisposed()) {
            return;
        }
        setupClickListener();
        setText("Jenv");
//        subscribeJdkChangeEvents(statusBar);
//        update(statusBar.getWidget(ID));
    }

    private void setupClickListener() {
        ClickListener clickListener = new ClickListener() {
            @Override
            public boolean onClick(@NotNull MouseEvent event, int clickCount) {
                if (!project.isDisposed()) {
//                    showPopup();
                    System.out.println("Status Bar 被点击了 ................... ");
                }
                return true;
            }
        };
        clickListener.installOn(this);
    }

    private void showPopup() {

    }

    private void subscribeJdkChangeEvents(StatusBar statusBar) {


    }


    @Override
    public void dispose() {

    }
}
