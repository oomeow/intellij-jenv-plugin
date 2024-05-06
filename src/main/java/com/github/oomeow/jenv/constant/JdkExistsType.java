package com.github.oomeow.jenv.constant;

import com.intellij.icons.AllIcons;

import javax.swing.*;

public enum JdkExistsType {
    JEnvHomePathInvalid("Invalid home path, this jEnv JDK has removed", AllIcons.General.Error),
    OnlyInJEnv("Only exists in jEnv", null),
    // The following belong to IDEA
    OnlyInIDEA("Only exists in IDEA", null),
    Both("Exist in jEnv and IDEA", null),
    OnlyMajorVersionMatch("Not jEnv, but Java major version exist in jEnv", null),
    OnlyNameNotMatch("Same one, but different name", AllIcons.General.Warning),
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
