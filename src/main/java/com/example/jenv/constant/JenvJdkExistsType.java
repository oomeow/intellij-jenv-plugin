package com.example.jenv.constant;

import com.example.jenv.icons.JenvIcons;
import com.intellij.icons.AllIcons;

import javax.swing.*;

public enum JenvJdkExistsType {
    Jenv("This jdk is only exists in Jenv", AllIcons.General.Error),
    Idea("This jdk is exists in idea", JenvIcons.IDEA),
    Both("This jdk is exists in idea and Jenv", null),
    OnlyMajorVersionMatch("The major version of this jdk exists in Idea and Jenv, but this is not Jenv jdk", AllIcons.General.Warning),
    OnlyNameNotMatch("The name of this jdk is exists in Idea and Jenv, but only name not match", AllIcons.General.Warning),
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
