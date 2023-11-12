package com.example.jenv.constant;

import com.intellij.icons.AllIcons;

import javax.swing.*;

public enum JenvJdkExistsType {
    Jenv("This jdk is only exists in Jenv", null),
    Idea("This jdk is exists in idea", null),
    Both("This jdk is exists in idea and Jenv", null),
    OnlyMajorVersionMatch("The major version of this jdk exists in Idea and Jenv, but this is not Jenv jdk", null),
    OnlyHomePathMatch("The home path of this jdk exists in Idea and Jenv, this jdk is belong Idea and Jenv", null),
    OnlyNameNotMatch("The name of this jdk is exists in Idea and Jenv, but only name not match", null),
    ;
    /*
     * [遍历 ideaJdks]
     *
     * version 存在(修改.java-version)
     *      - path 存在  ==> ExistsInBoth
     *      - path 不存在  ==> VersionInIdea --- 提示路径不对
     *
     * version 不存在 --- 提示不修改 .java-version，使用idea的jdk
     *      - path 存在  ==>
     *      - path 不存在
     *          - 判断使用的 java 版本 getVersion
     *              - 版本存在 -- 修改 .java-version
     **/


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
