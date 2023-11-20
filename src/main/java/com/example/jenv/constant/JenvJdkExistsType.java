package com.example.jenv.constant;

import com.intellij.icons.AllIcons;

import javax.swing.*;

public enum JenvJdkExistsType {
    Jenv("This JDK is only exists in Jenv", null),
    // The following belong to IDEA
    Idea("This JDK is existing in idea", null),
    Both("This JDK is existing in idea and Jenv", null),
    OnlyMajorVersionMatch("The major version of this JDK is existing in Idea and Jenv, but this is not Jenv JDK", AllIcons.General.Warning),
    OnlyNameNotMatch("The name of this JDK is existing in Idea and Jenv, but only name not match", AllIcons.General.Warning),
    ;

    private String description;
    private Icon icon;

    JenvJdkExistsType(String description, Icon icon) {
        this.description = description;
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }
}
