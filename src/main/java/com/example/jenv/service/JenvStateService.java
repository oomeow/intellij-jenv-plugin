package com.example.jenv.service;

import com.example.jenv.config.JenvState;
import com.intellij.openapi.application.ApplicationManager;

public class JenvStateService {

    private final JenvState state;

    JenvStateService() {
        state = new JenvState();
    }

    public static JenvStateService getInstance() {
        return ApplicationManager.getApplication().getService(JenvStateService.class);
    }

    public JenvState getState() {
        return state;
    }
}
