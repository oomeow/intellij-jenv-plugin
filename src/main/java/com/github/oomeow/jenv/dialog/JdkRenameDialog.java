package com.github.oomeow.jenv.dialog;

import com.github.oomeow.jenv.model.JenvJdkModel;
import com.github.oomeow.jenv.model.JenvRenameModel;
import com.github.oomeow.jenv.service.JenvJdkTableService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class JdkRenameDialog extends DialogWrapper {
    private final List<JenvRenameModel> renameModelList;
    private final List<String> ideaNameList = new ArrayList<>();
    private final List<String> jenvNameList = new ArrayList<>();
    private final List<ComponentValidator> validators = new ArrayList<>();

    public JdkRenameDialog(Project project, List<JenvRenameModel> renameModelList) {
        super(project, true);
        this.renameModelList = renameModelList;
        init();
        setTitle("JDK Rename");
        setSize(400, getSize().height);
        for (JenvJdkModel jenvJdkModel : JenvJdkTableService.getInstance().getAllIdeaJdks()) {
            ideaNameList.add(jenvJdkModel.getName());
        }
        List<JenvJdkModel> allJenvJdkFiles = JenvJdkTableService.getInstance().getAllJenvJdkFiles();
        for (JenvJdkModel jenvJdkFile : allJenvJdkFiles) {
            jenvNameList.add(jenvJdkFile.getName());
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        int size = renameModelList.size();
        int row = 0;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(size + 3, 2);
        JPanel panel = new JPanel(gridLayoutManager);
        // row 1 -> tip
        JLabel tipLabel = new JLabel("JDKs that belong to jEnv have their own names and not editable");
        tipLabel.setFont(new Font("Arial", Font.BOLD, 14));
        tipLabel.setForeground(JBColor.gray);
        tipLabel.setIcon(AllIcons.General.Information);
        GridConstraints tip2GirdConst = new GridConstraints();
        tip2GirdConst.setRow(row);
        tip2GirdConst.setColumn(0);
        tip2GirdConst.setColSpan(2);
        tip2GirdConst.setAnchor(GridConstraints.ANCHOR_WEST);
        panel.add(tipLabel, tip2GirdConst);
        // row 2 -> space
        row += 1;
        GridConstraints spacerConstraints = new GridConstraints();
        spacerConstraints.setRow(row);
        panel.add(new Spacer(), spacerConstraints);
        // row 3 -> title
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
        // from row 4 -> generator rename
        for (int i = 0; i < size; i++) {
            JenvRenameModel jenvRenameModel = renameModelList.get(i);
            row = row + 1;
            int col = 0;
            Sdk sdk = jenvRenameModel.getIdeaSdk();
            // label
            JLabel jdkNameLabel = new JLabel(sdk.getName());
            GridConstraints jdkNameConstraints = new GridConstraints();
            jdkNameConstraints.setRow(row);
            jdkNameConstraints.setColumn(col);
            jdkNameConstraints.setAnchor(GridConstraints.ANCHOR_WEST);
            panel.add(jdkNameLabel, jdkNameConstraints);
            // textFiled
            JBTextField textField = new JBTextField();
            textField.setToolTipText(jenvRenameModel.getIdeaSdk().getVersionString());
            textField.setHorizontalAlignment(SwingConstants.CENTER);
            GridConstraints editConstraints = new GridConstraints();
            editConstraints.setRow(row);
            editConstraints.setColumn(col + 1);
            editConstraints.setAnchor(GridConstraints.ANCHOR_WEST);
            editConstraints.setFill(GridConstraints.FILL_HORIZONTAL);
            editConstraints.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
            // validator
            Supplier<ValidationInfo> componentValidatorConsumer = getValidationInfoSupplier(jenvRenameModel, textField);
            ComponentValidator componentValidator = new ComponentValidator(this.getDisposable()).withValidator(componentValidatorConsumer).installOn(textField);
            validators.add(componentValidator);
            textField.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(@NotNull DocumentEvent e) {
                    jenvRenameModel.setChangeName(textField.getText().trim());
                    ComponentValidator.getInstance(textField).ifPresent(o -> validators.forEach(ComponentValidator::revalidate));
                    setOKActionEnabled(validators.stream().allMatch(o -> o.getValidationInfo() == null));
                }
            });
            if (jenvRenameModel.isBelongJenv()) {
                textField.setText(jenvRenameModel.getChangeName());
                textField.setEnabled(false);
            } else {
                setOKActionEnabled(false);
            }
            panel.add(textField, editConstraints);
        }
        return panel;
    }

    @NotNull
    private Supplier<ValidationInfo> getValidationInfoSupplier(JenvRenameModel jenvRenameModel, JBTextField textField) {
        return () -> {
            StringBuilder builder = new StringBuilder();
            Map<String, JenvRenameModel> map = new HashMap<>();
            renameModelList.stream().filter(o -> !o.equals(jenvRenameModel)).forEach(o -> {
                String changeName = o.getChangeName();
                map.put(changeName, o);
            });
            if (jenvRenameModel.isBelongJenv()) {
                return null;
            }
            String name = jenvRenameModel.getIdeaSdk().getName();
            String changeName = jenvRenameModel.getChangeName();
            if (StringUtils.isEmpty(changeName)) {
                builder.append("The change name is empty.<br>");
            } else if (!jenvRenameModel.isBelongJenv() && jenvNameList.contains(changeName)) {
                builder.append("The change name ").append("[").append(changeName).append("] will add as jenv JDK.<br>");
            } else if (changeName.equals(name)) {
                builder.append("The change name not to same.<br>");
            } else if (ideaNameList.contains(changeName)) {
                builder.append("The change name ").append("[").append(changeName).append("] has exists in IDEA.<br>");
            } else if (map.get(changeName) != null) {
                builder.append("Multiple the same name ").append("[").append(changeName).append("].<br>");
            }
            if (!builder.toString().trim().isEmpty()) {
                return new ValidationInfo(builder.toString(), textField);
            }
            return null;
        };
    }

    @Override
    protected void doOKAction() {
        ApplicationManager.getApplication().invokeAndWait(() -> ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                for (JenvRenameModel jenvRenameModel : renameModelList) {
                    Sdk ideaSdk = jenvRenameModel.getIdeaSdk();
                    Sdk clone = (Sdk) ideaSdk.clone();
                    SdkModificator sdkModificator = clone.getSdkModificator();
                    sdkModificator.setName(jenvRenameModel.getChangeName());
                    sdkModificator.commitChanges();
                    ProjectJdkTable.getInstance().updateJdk(ideaSdk, clone);
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            super.doOKAction();
        }));
    }

}
