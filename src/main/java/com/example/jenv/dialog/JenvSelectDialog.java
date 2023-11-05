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
    private boolean isJenvJdkSelected;
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
        JenvService jenvService = JenvService.getInstance();
        JenvStateService jenvStateService = project.getService(JenvStateService.class);
        ProjectJenvState state = Objects.requireNonNull(jenvStateService.getState());
        state.setCurrentJavaVersion(jdkVersion);
        state.setChangeJdkByDialog(true);
        state.setJenvJdkSelected(isJenvJdkSelected);
        jenvService.changeJenvVersion(project, state);
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        radioButton_jenv = new JRadioButton();
        radioButton_other = new JRadioButton();
        String[] jenvJdksArr = JenvHelper.getJenvIdeaJdkVersionList().toArray(new String[0]);
        comboBox_jenv = new ComboBox<>(jenvJdksArr);
        String[] notJenvJdkArr = JenvHelper.getNotJenvIdeaJdkVersionList().toArray(new String[0]);
        comboBox_other = new ComboBox<>(notJenvJdkArr);

//      Components status init
        ProjectJenvState state = project.getService(JenvStateService.class).getState();
        jdkVersion = state.getCurrentJavaVersion();
        boolean jdkExists = JenvHelper.checkIdeaJDKExists(jdkVersion);
        if (jdkExists) {
            boolean isJenvJdk = JenvHelper.checkIsJenvJdk(jdkVersion);
            if (isJenvJdk) {
                radioButton_jenv.setSelected(true);
                comboBox_jenv.setSelectedIndex(JenvHelper.getCurrentJdkVersionPosition(jdkVersion, true, false));
                comboBox_other.setEnabled(false);
                isJenvJdkSelected = true;
            } else {
                radioButton_other.setSelected(true);
                comboBox_other.setSelectedIndex(JenvHelper.getCurrentJdkVersionPosition(jdkVersion, false, true));
                comboBox_jenv.setEnabled(false);
                isJenvJdkSelected = false;
            }
        }

//      RadioButton status init
        radioButton_jenv.addChangeListener(item -> {
            if (radioButton_jenv.isSelected()) {
                if (!comboBox_jenv.isEnabled()) {
                    comboBox_jenv.setEnabled(true);
                    comboBox_other.setEnabled(false);
                    String selectedItem = (String) comboBox_jenv.getSelectedItem();
                    if (selectedItem != null && !StringUtils.equals(jdkVersion, selectedItem)) {
                        jdkVersion = selectedItem;
                        isJenvJdkSelected = true;
                    }
                }
            }
        });
        radioButton_other.addChangeListener(item -> {
            if (radioButton_other.isSelected()) {
                if (!comboBox_other.isEnabled()) {
                    comboBox_other.setEnabled(true);
                    comboBox_jenv.setEnabled(false);
                    String selectedItem = (String) comboBox_other.getSelectedItem();
                    if (selectedItem != null && !StringUtils.equals(jdkVersion, selectedItem)) {
                        jdkVersion = selectedItem;
                        isJenvJdkSelected = false;
                    }
                }
            }
        });

//      ComboBox status init
        comboBox_jenv.addActionListener(item -> {
            ComboBox<String> jenvComboBoxItem = (ComboBox<String>) item.getSource();
            System.out.println("jenvComboBoxItem = " + jenvComboBoxItem.getItem());
            jdkVersion = jenvComboBoxItem.getItem();
            isJenvJdkSelected = true;
        });
        comboBox_other.addActionListener(item -> {
            ComboBox<String> otherComboBoxItem = (ComboBox<String>) item.getSource();
            System.out.println("otherComboBoxItem = " + otherComboBoxItem.getItem());
            jdkVersion = otherComboBoxItem.getItem();
            isJenvJdkSelected = false;
        });
    }
}
