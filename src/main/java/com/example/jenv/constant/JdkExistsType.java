package com.example.jenv.constant;

import com.intellij.icons.AllIcons;

import javax.swing.*;

public enum JdkExistsType {
    Jenv("Exists in Jenv", null),
    // The following belong to IDEA
    IDEA("Exists in IDEA", null),
    Both("Exists in Jenv and IDEA", null),
    OnlyMajorVersionMatch("Not Jenv, Java Major Version Exists in Jenv", AllIcons.General.Warning),
    OnlyNameNotMatch("Same JDK path, Different Name", AllIcons.General.Warning),
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
