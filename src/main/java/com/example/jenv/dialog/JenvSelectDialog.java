package com.example.jenv.dialog;

import com.example.jenv.JenvHelper;
import com.example.jenv.config.ProjectJenvState;
import com.example.jenv.service.JenvService;
import com.example.jenv.service.JenvStateService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class JenvSelectDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> comboBox_jenv;
    private JComboBox<String> comboBox_other;
    private JRadioButton radioButton_jenv;
    private JRadioButton radioButton_other;

    private String jdkVersion;
    private final Project project;

    public JenvSelectDialog(Project project, String title) {
        this.project = project;
        setTitle(title);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void onOK() {
        JenvStateService jenvStateService = project.getService(JenvStateService.class);
        ProjectJenvState state = Objects.requireNonNull(jenvStateService.getState());
        state.setCurrentJavaVersion(jdkVersion);
        state.setFileHasChange(false);
        JenvService.getInstance().changeJenvJdkWithNotification(project, jdkVersion, state);
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        radioButton_jenv = new JRadioButton("Jenv Jdks");
        radioButton_other = new JRadioButton("Other Jdks");
        String[] jenvJdksArr = JenvHelper.getIdeaJdkVersionList(true).toArray(new String[0]);
        comboBox_jenv = new ComboBox<>(jenvJdksArr);
        String[] notJenvJdkArr = JenvHelper.getIdeaJdkVersionList(false).toArray(new String[0]);
        comboBox_other = new ComboBox<>(notJenvJdkArr);

        // Components status init
        ProjectJenvState state = project.getService(JenvStateService.class).getState();
        jdkVersion = state.getCurrentJavaVersion();
        boolean jdkExists = JenvHelper.checkIdeaJdkExistsByVersion(jdkVersion);
        if (jdkExists) {
            boolean isJenvJdk = JenvHelper.checkIsJenvJdk(jdkVersion);
            if (isJenvJdk) {
                radioButton_jenv.setSelected(true);
                comboBox_jenv.setSelectedIndex(JenvHelper.getCurrentJdkVersionPosition(jdkVersion, true));
                comboBox_other.setEnabled(false);
            } else {
                radioButton_other.setSelected(true);
                comboBox_other.setSelectedIndex(JenvHelper.getCurrentJdkVersionPosition(jdkVersion, false));
                comboBox_jenv.setEnabled(false);
            }
        } else {
            radioButton_jenv.setSelected(true);
            comboBox_jenv.setSelectedItem(0);
            comboBox_other.setEnabled(false);
        }

        // RadioButton status init
        radioButton_jenv.addChangeListener(o -> {
            if (radioButton_jenv.isSelected()) {
                if (!comboBox_jenv.isEnabled()) {
                    comboBox_jenv.setEnabled(true);
                    comboBox_other.setEnabled(false);
                    String selectedItem = (String) comboBox_jenv.getSelectedItem();
                    if (selectedItem != null && !StringUtils.equals(jdkVersion, selectedItem)) {
                        jdkVersion = selectedItem;
                    }
                }
            }
        });
        radioButton_other.addChangeListener(o -> {
            if (radioButton_other.isSelected()) {
                if (!comboBox_other.isEnabled()) {
                    comboBox_other.setEnabled(true);
                    comboBox_jenv.setEnabled(false);
                    String selectedItem = (String) comboBox_other.getSelectedItem();
                    if (selectedItem != null && !StringUtils.equals(jdkVersion, selectedItem)) {
                        jdkVersion = selectedItem;
                    }
                }
            }
        });

        // ComboBox status init
        comboBox_jenv.addActionListener(o -> {
            Object source = o.getSource();
            if (source instanceof ComboBox<?> comboBox) {
                Object item = comboBox.getItem();
                if (item instanceof String jdkVersionItem) {
                    jdkVersion = jdkVersionItem;
                }
            }
        });
        comboBox_other.addActionListener(o -> {
            Object source = o.getSource();
            if (source instanceof ComboBox<?> comboBox) {
                Object item = comboBox.getItem();
                if (item instanceof String jdkVersionItem) {
                    jdkVersion = jdkVersionItem;
                }
            }
        });
        // String tips = "If you select other jdks, the project jdk still change, but the Jenv version file not be change.";
    }
}
