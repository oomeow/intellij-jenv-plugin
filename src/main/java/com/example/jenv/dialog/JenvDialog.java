package com.example.jenv.dialog;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.JenvState;
import com.example.jenv.constant.DialogMessage;
import com.example.jenv.service.JenvService;
import com.example.jenv.service.JenvStateService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JenvDialog extends AbstractDialogWrapper {

    private ComboBox<String> comboBox;

    public JenvDialog(DialogMessage dialogMessage) {
        super(dialogMessage);
        setTitle(dialogMessage.getTitle());
    }

    @Override
    protected void checkJenvConfig() {
        JenvState state = Objects.requireNonNull(JenvStateService.getInstance().getState());
        Integer position = JenvHelper.getCurrentVersionPosition(state.getFormattedJavaVersion());
        comboBox.setSelectedIndex(position == null ? 0 : position);
    }

    @Override
    protected void updateJenvConfig() {
        String selectedVersion = String.valueOf(comboBox.getSelectedItem());
        JenvState state = Objects.requireNonNull(JenvStateService.getInstance().getState());
        state.setCurrentJavaVersion(selectedVersion);
        state.setChangeJenvByDialog(true);
        JenvService service = ApplicationManager.getApplication().getService(JenvService.class);
        service.changeJenvVersion();
        this.close(OK_EXIT_CODE);
    }

    @Override
    protected Map<String, JComponent> makeComponents() {
        List<String> versionList = JenvHelper.getAllJdkVersionList();
        comboBox = new ComboBox<>(versionList.toArray(new String[0]));
        return new HashMap<>() {{
            put("versionBox", comboBox);
        }};
    }

    @Override
    protected void attachComponents() {
        componentMap.forEach((name, component) -> panel.add(component, BorderLayout.CENTER));
    }


}
