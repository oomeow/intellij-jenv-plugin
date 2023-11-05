package com.example.jenv.dialog;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.ProjectJenvState;
import com.example.jenv.constant.DialogMessage;
import com.example.jenv.service.JenvService;
import com.example.jenv.service.JenvStateService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JenvDialog extends AbstractDialogWrapper {

    private ComboBox<String> comboBox;

    public JenvDialog(DialogMessage dialogMessage, Project project) {
        super(dialogMessage, project);
        setTitle(dialogMessage.getTitle());
    }

    @Override
    protected void checkJenvConfig() {
        ProjectJenvState state = Objects.requireNonNull(project.getService(JenvStateService.class).getState());
        Integer position = JenvHelper.getCurrentJdkVersionPosition(state.getCurrentJavaVersion(), false, false);
        comboBox.setSelectedIndex(position == null ? 0 : position);
    }

    @Override
    protected void updateJenvConfig(Project project) {
        JenvStateService jenvStateService = project.getService(JenvStateService.class);
        String selectedVersion = String.valueOf(comboBox.getSelectedItem());
        ProjectJenvState state = jenvStateService.getState();
        state.setCurrentJavaVersion(selectedVersion);
        state.setChangeJdkByDialog(true);
        state.setJenvJdkSelected(true);
        JenvService service = JenvService.getInstance();
        service.changeJenvVersion(project, state);
        this.close(OK_EXIT_CODE);
    }

    @Override
    protected Map<String, JComponent> makeComponents() {
        List<String> versionList = JenvHelper.getJenvIdeaJdkVersionList();
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
