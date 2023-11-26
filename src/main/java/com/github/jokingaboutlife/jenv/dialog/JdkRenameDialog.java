package com.github.jokingaboutlife.jenv.dialog;

import com.github.jokingaboutlife.jenv.constant.JenvConstants;
import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.model.JenvRenameModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdkRenameDialog extends DialogWrapper {
    private final List<Sdk> addJdkList;
    private final List<JenvRenameModel> renameModelList;
    private final List<String> existsNameList = new ArrayList<>();

    public JdkRenameDialog(Project project, List<JenvRenameModel> renameModelList, List<Sdk> addJdkList) {
        super(project, true);
        this.addJdkList = addJdkList;
        this.renameModelList = renameModelList;
        init();
        setTitle("JDK Rename");
        setSize(400, getSize().height);
        for (Sdk sdk : ProjectJdkTable.getInstance().getAllJdks()) {
            existsNameList.add(sdk.getName());
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        int size = renameModelList.size();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(size + 1, 2, JBUI.insets(5), -1, -1);
        JPanel panel = new JPanel(gridLayoutManager);
        // notify message
//        String subTitle = "Some IDEA JDK need to rename, you can choose what JDK be renamed, Belong to Jenv JDK has its name, and it is not being edited.";
//        JLabel subTitleLabel = new JLabel(subTitle);
//        GridConstraints subTitleGridC = new GridConstraints(0, 0, 1, 1,
//                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
//                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
//                null, null, null, 0, false);
//        panel.add(subTitleLabel, subTitleGridC);
        // column name
        JLabel nameLabel = new JLabel("Before name");
        JLabel renameLabel = new JLabel("After name");
        panel.add(nameLabel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        panel.add(renameLabel, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        // generator rename row
        for (int i = 0; i < size; i++) {
            JenvRenameModel jenvRenameModel = renameModelList.get(i);
            int row = i + 1;
            int col = 0;
            GridConstraints fileConstraints = new GridConstraints(row, col, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false);
            JBTextField textField = new JBTextField();
            GridConstraints editConstraints = new GridConstraints(row, col + 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null, 0, false);
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    jenvRenameModel.setChangeName(textField.getText().trim());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    jenvRenameModel.setChangeName(textField.getText().trim());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    // use to a Plain Text component, not applicable to this component.
                }
            });
            Sdk sdk = jenvRenameModel.getIdeaSdk();
            JBCheckBox sdkNameCheckBox = new JBCheckBox(sdk.getName(), true);
            sdkNameCheckBox.addChangeListener(e -> {
                if (e.getSource() instanceof JBCheckBox checkBox) {
                    jenvRenameModel.setSelected(checkBox.isSelected());
                    if (!jenvRenameModel.isBelongJenv()) {
                        textField.setEnabled(checkBox.isSelected());
                    }
                }
            });
            if (jenvRenameModel.isBelongJenv()) {
                textField.setText(jenvRenameModel.getChangeName());
                textField.setEnabled(false);
            }
            panel.add(sdkNameCheckBox, fileConstraints);
            panel.add(textField, editConstraints);
        }
        return panel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        StringBuilder builder = new StringBuilder();
        Map<String, JenvRenameModel> map = new HashMap<>();
        for (JenvRenameModel jenvRenameModel : renameModelList) {
            if (!jenvRenameModel.isSelected()) {
                continue;
            }
            String name = jenvRenameModel.getIdeaSdk().getName();
            String changeName = jenvRenameModel.getChangeName();
            if (StringUtils.isEmpty(changeName)) {
                builder.append("JDK Name: ").append(name).append(" rename is empty.<br>");
                continue;
            }
            if (changeName.equals(name)) {
                builder.append("JDK Name: ").append(name).append(" rename not to same.<br>");
                continue;
            }
            if (existsNameList.contains(changeName)) {
                builder.append("JDK Name: ").append(name).append(": the change name ").append("[").append(changeName).append("] has exists in IDEA.<br>");
                continue;
            }
            if (map.get(changeName) != null) {
                builder.append("JDK Name: ").append(name).append(" More than one of the same name ").append("[").append(changeName).append("].<br>");
            }
            map.put(changeName, jenvRenameModel);
        }
        ValidationInfo validationInfo = null;
        if (StringUtils.isNotEmpty(builder)) {
            builder.insert(0, "<html>");
            builder.append("</html>");
            String str = builder.toString();
            validationInfo = new ValidationInfo(str);
        }
        // return null mean success
        return validationInfo;
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeAndWait(() -> ApplicationManager.getApplication().runWriteAction(() -> {
            for (JenvRenameModel jenvRenameModel : renameModelList) {
                if (!jenvRenameModel.isSelected()) {
                    continue;
                }
                // change IDEA SDK name
                Sdk ideaSdk = jenvRenameModel.getIdeaSdk();
                SdkModificator sdkModificator = ideaSdk.getSdkModificator();
                sdkModificator.setName(jenvRenameModel.getChangeName());
                ProjectJdkTable.getInstance().updateJdk(ideaSdk, (Sdk) sdkModificator);
                if (!jenvRenameModel.isBelongJenv()) {
                    // add Jenv jdk to IDEA
                    JenvJdkModel jenvJdk = jenvRenameModel.getJenvJdk();
                    VirtualFile homePath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jenvJdk.getHomePath()));
                    if (homePath != null) {
                        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
                        Sdk sdk = SdkConfigurationUtil.setupSdk(allJdks, homePath, JenvConstants.PROJECT_JENV_JDK_TYPE, true, null, jenvJdk.getName());
                        if (sdk != null) {
                            addJdkList.add(sdk);
                        }
                    }
                }
            }
            super.doOKAction();
        }));
    }

}
