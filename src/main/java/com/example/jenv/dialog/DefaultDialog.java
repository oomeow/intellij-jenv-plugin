package com.example.jenv.dialog;


import com.example.jenv.constant.DialogMessage;
import com.example.jenv.constant.JenvConstants;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DefaultDialog extends AbstractDialogWrapper {
    public DefaultDialog(DialogMessage dialogMessage) {
        super(dialogMessage);
        setTitle(dialogMessage.getTitle());
    }

    @Override
    protected void checkJenvConfig() {
    }

    @Override
    protected void updateJenvConfig() {
    }

    @Override
    protected Map<String, JComponent> makeComponents() {
        return new HashMap<>() {{
            put("guideLabel", new JLabel(dialogMessage.getDescription()));
        }};
    }

    @Override
    protected void doOKAction() {
        try {
            if (dialogMessage == DialogMessage.JENV_NOT_INSTALL) {
                Desktop.getDesktop().browse(new URI(JenvConstants.JENV_INSTALL_URL.getName()));
            } else {
                this.close(OK_EXIT_CODE);
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    protected void attachComponents() {
        componentMap.forEach((name, component) -> panel.add(component, BorderLayout.CENTER));
    }
}