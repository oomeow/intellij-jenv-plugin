package com.example.jenv.constant;

public enum JenvConstants {
    VERSION_FILE(".java-version"),
    JENV_FILE_EXTENSION(".jenv"),
    JENV_INSTALL_URL("https://www.jenv.be");

    private final String name;

    JenvConstants(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}