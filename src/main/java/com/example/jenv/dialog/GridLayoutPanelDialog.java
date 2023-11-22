package com.example.jenv.dialog;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.dsl.gridLayout.GridLayout;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridLayoutPanelDialog extends DialogWrapper {
    private List<Sdk> sdks;
    private List<JBTextField> fileEditList = new ArrayList<>();

    public GridLayoutPanelDialog(@Nullable Project project, List<Sdk> sdks) {
        super(project, true);
        this.sdks = sdks;
        for (int i = 0; i < sdks.size(); i++) {
            fileEditList.add(new JBTextField());
        }
        setTitle("JDK Rename");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        int size = sdks.size();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(size, 2, JBUI.insets(5), -1, -1);
        JPanel panel = new JPanel(gridLayoutManager);
        for (int i = 0; i < size; i++) {
            int col = 0;
            // 添加文本编辑框到面板
            GridConstraints fileConstraints = new GridConstraints(i, col, 1, 1,
                    GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false);
            Sdk sdk = sdks.get(i);
            panel.add(new JLabel("JDK Name: " + sdk.getName()), fileConstraints);
            // 文本编辑框
            JBTextField textField = fileEditList.get(i);
            GridConstraints editConstraints = new GridConstraints(i, col + 1, 1, 1,
                    GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false);
            panel.add(textField, editConstraints);
        }
        return panel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        StringBuilder builder = new StringBuilder("<html>");
        Map<String, Sdk> map = new HashMap<>();
        for (int i = 0; i < fileEditList.size(); i++) {
            String name = sdks.get(i).getName();
            JBTextField textField = fileEditList.get(i);
            String text = textField.getText().trim();
            if (StringUtils.isEmpty(text)) {
                builder.append("JDK Name: ").append(name).append(" rename is empty.<br>");
                continue;
            }
            if (name.equals(text)) {
                builder.append("JDK Name: ").append(name).append(" rename not to same.<br>");
                continue;
            }
            if (map.get(text) != null) {
                builder.append("JDK Name: ").append(name).append(" More than one of the same name ").append("[").append(text).append("].<br>");
            }
            map.put(text, sdks.get(i));
        }
        builder.append("</html>");
        String str = builder.toString();
        ValidationInfo validationInfo = null;
        if (StringUtils.isNotEmpty(str)) {
            validationInfo = new ValidationInfo(str);
        }
        // 在这里可以进行输入验证，返回 null 表示验证通过
        return validationInfo;
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            System.out.println("OK OK OK");
            super.doOKAction();
        });
    }
}
