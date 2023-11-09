package com.example.jenv.model;

import com.example.jenv.constant.JenvJdkExistsType;
import com.intellij.openapi.projectRoots.Sdk;

public class JenvSdkModel {
    private String name;
    private String version;
    private String majorVersion;
    private String homePath;
    private String canonicalPath;
//    private boolean belongIdea;
//    private boolean belongJenv;
    private JenvJdkExistsType existsType;
    private Sdk ideaJdkInfo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(String majorVersion) {
        this.majorVersion = majorVersion;
    }

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }

    public void setCanonicalPath(String canonicalPath) {
        this.canonicalPath = canonicalPath;
    }

//    public boolean isBelongIdea() {
//        return belongIdea;
//    }
//
//    public void setBelongIdea(boolean belongIdea) {
//        this.belongIdea = belongIdea;
//    }
//
//    public boolean isBelongJenv() {
//        return belongJenv;
//    }
//
//    public void setBelongJenv(boolean belongJenv) {
//        this.belongJenv = belongJenv;
//    }


    public JenvJdkExistsType getExistsType() {
        return existsType;
    }

    public void setExistsType(JenvJdkExistsType existsType) {
        this.existsType = existsType;
    }

    public Sdk getIdeaJdkInfo() {
        return ideaJdkInfo;
    }

    public void setIdeaJdkInfo(Sdk ideaJdkInfo) {
        this.ideaJdkInfo = ideaJdkInfo;
    }
}
