package com.example.jenv.dialog;

import com.example.jenv.constant.JenvConstants;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class JdkRenameDialog extends DialogWrapper {
    private final List<Sdk> sdkList;

    public JdkRenameDialog(List<Sdk> updateJdkNameList) {
        super(true);
        this.sdkList = updateJdkNameList;
        setTitle("JDK Rename");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        JTextPane textPane = new JTextPane();
        StringBuilder sb = new StringBuilder();
        sb.append("This jdk name will be rename:\n");
        for (Sdk sdk : sdkList) {
            sb.append(sdk.getName()).append(" ---> ").append(sdk.getName()).append(JenvConstants.JDK_RENAME_SUFFIX).append("\n");
        }
        textPane.setText(sb.toString());
        dialogPanel.add(textPane, BorderLayout.CENTER);
        return dialogPanel;
    }

}
