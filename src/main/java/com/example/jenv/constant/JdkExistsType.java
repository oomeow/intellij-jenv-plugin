package com.example.jenv.constant;

import javax.swing.*;

public enum JdkExistsType {
    Jenv("Exists in Jenv", null),
    // The following belong to IDEA
    IDEA("Exists in IDEA", null),
    Both("Exists in Jenv and IDEA", null),
    OnlyMajorVersionMatch("Not Jenv, Java Major Version Exists in Jenv", null),
    OnlyNameNotMatch("Same JDK path, Different Name", null),
    ;

    private final String description;
    private final Icon icon;

    JdkExistsType(String description, Icon icon) {
        this.description = description;
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public Icon getIcon() {
        return icon;
    }

}
