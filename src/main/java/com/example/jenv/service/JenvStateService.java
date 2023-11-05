package com.example.jenv.service;

import com.example.jenv.config.ProjectJenvState;

public class JenvStateService {

    private final ProjectJenvState state;

    JenvStateService() {
        state = new ProjectJenvState();
    }

    public ProjectJenvState getState() {
        return state;
    }
}
