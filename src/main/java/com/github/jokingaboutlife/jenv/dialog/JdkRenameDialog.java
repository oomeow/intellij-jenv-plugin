package com.github.jokingaboutlife.jenv.dialog;

import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.model.JenvRenameModel;
import com.github.jokingaboutlife.jenv.service.JenvJdkTableService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdkRenameDialog extends DialogWrapper {
    private final List<Sdk> addJdkList;
    private final List<JenvRenameModel> renameModelList;
    private final List<String> existsNameList = new ArrayList<>();
    private final List<String> jenvNameList = new ArrayList<>();

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
        List<JenvJdkModel> allJenvJdks = JenvJdkTableService.getInstance().getAllJenvJdks();
        for (JenvJdkModel jenvJdkModel : allJenvJdks) {
            jenvNameList.add(jenvJdkModel.getName());
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        int size = renameModelList.size();
        int row = 0;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(size + 4, 2);
        JPanel panel = new JPanel(gridLayoutManager);
        // row 1
        // notify message
        Font font = new Font("Arial", Font.BOLD, 14);
        JLabel tip1Label = new JLabel("Choose JDK to update name.");
        tip1Label.setFont(font);
        tip1Label.setForeground(JBColor.gray);
        tip1Label.setIcon(AllIcons.General.Information);
        GridConstraints tip1GirdConst = new GridConstraints();
        tip1GirdConst.setRow(row);
        tip1GirdConst.setColumn(0);
        tip1GirdConst.setColSpan(2);
        tip1GirdConst.setAnchor(GridConstraints.ANCHOR_WEST);
        panel.add(tip1Label, tip1GirdConst);
        // row 2
        row += 1;
        JLabel tip2Label = new JLabel("Belong to jEnv JDK has its name and it is not be edited.");
        tip2Label.setFont(font);
        tip2Label.setForeground(JBColor.gray);
        tip2Label.setIcon(AllIcons.General.Information);
        GridConstraints tip2GirdConst = new GridConstraints();
        tip2GirdConst.setRow(row);
        tip2GirdConst.setColumn(0);
        tip2GirdConst.setColSpan(2);
        tip2GirdConst.setAnchor(GridConstraints.ANCHOR_WEST);
        panel.add(tip2Label, tip2GirdConst);
        // row 3
        row += 1;
        GridConstraints spacerConstraints = new GridConstraints();
        spacerConstraints.setRow(row);
        panel.add(new Spacer(), spacerConstraints);
        // row 4
        row += 1;
        JLabel nameLabel = new JLabel("Before");
        GridConstraints nameGridConstraints = new GridConstraints();
        nameGridConstraints.setRow(row);
        nameGridConstraints.setColumn(0);
        nameGridConstraints.setAnchor(GridConstraints.ANCHOR_WEST);
        JLabel renameLabel = new JLabel("After");
        GridConstraints renameGridConstraints = new GridConstraints();
        renameGridConstraints.setRow(row);
        renameGridConstraints.setColumn(1);
        renameGridConstraints.setAnchor(GridConstraints.ANCHOR_CENTER);
        panel.add(nameLabel, nameGridConstraints);
        panel.add(renameLabel, renameGridConstraints);
        // generator rename row from 5
        for (int i = 0; i < size; i++) {
            JenvRenameModel jenvRenameModel = renameModelList.get(i);
            row = row + 1;
            int col = 0;
            GridConstraints checkBoxConstraints = new GridConstraints();
            checkBoxConstraints.setRow(row);
            checkBoxConstraints.setColumn(col);
            checkBoxConstraints.setAnchor(GridConstraints.ANCHOR_WEST);
            JBTextField textField = new JBTextField();
            GridConstraints editConstraints = new GridConstraints();
            editConstraints.setRow(row);
            editConstraints.setColumn(col + 1);
            editConstraints.setAnchor(GridConstraints.ANCHOR_WEST);
            editConstraints.setFill(GridConstraints.FILL_HORIZONTAL);
            editConstraints.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
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
                    // use to a Plain Text component, not applicable to this JBTextField.
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
            panel.add(sdkNameCheckBox, checkBoxConstraints);
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
                builder.append("[").append(name).append("]: ").append("rename is empty.<br>");
                continue;
            }
            if (changeName.equals(name)) {
                builder.append("[").append(name).append("]: ").append("rename not to same.<br>");
                continue;
            }
            if (existsNameList.contains(changeName)) {
                builder.append("[").append(name).append("]: ").append("the change name ").append("[").append(changeName).append("] has exists in IDEA.<br>");
                continue;
            }
            if (!jenvRenameModel.isBelongJenv() && jenvNameList.contains(changeName)) {
                builder.append("[").append(name).append("]: ").append("the change name ").append("[").append(changeName).append("] will add as jenv JDK.<br>");
                continue;
            }
            if (map.get(changeName) != null) {
                builder.append("[").append(name).append("]: ").append("More than one of the same name ").append("[").append(changeName).append("].<br>");
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
                    // add jEnv jdk to IDEA
                    JenvJdkModel jenvJdk = jenvRenameModel.getJenvJdk();
                    VirtualFile homePath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jenvJdk.getHomePath()));
                    if (homePath != null) {
                        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
                        Sdk sdk = SdkConfigurationUtil.setupSdk(allJdks, homePath, JavaSdk.getInstance(), true, null, jenvJdk.getName());
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
