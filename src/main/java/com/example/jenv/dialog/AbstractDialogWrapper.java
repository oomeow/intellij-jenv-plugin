package com.example.jenv.dialog;

import com.example.jenv.constant.DialogMessage;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public abstract class AbstractDialogWrapper extends DialogWrapper {
    protected static int WIDTH = 300;
    private static final String DEFAULT_TITLE = "Welcome to Jenv";

    protected JPanel panel;
    protected Map<String, JComponent> componentMap;
    protected DialogMessage dialogMessage;

    public AbstractDialogWrapper(DialogMessage dialogMessage) {
        super(true);
        this.dialogMessage = dialogMessage;
        setTitle(DEFAULT_TITLE);
        setResizable(false);
        componentMap = makeComponents();
        panel = new JPanel(new BorderLayout());
        checkJenvConfig();
        init();
    }

    protected abstract void checkJenvConfig();

    protected abstract void updateJenvConfig();

    protected abstract Map<String, JComponent> makeComponents();

    protected abstract void attachComponents();

    @Override
    protected void doOKAction() {
        updateJenvConfig();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        attachComponents();
        panel.setPreferredSize(new Dimension(WIDTH, panel.getHeight()));
        return panel;
    }
}
