package com.github.jokingaboutlife.jenv.config;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class JenvState {
    private boolean projectJenvExists;
    private String projectJenvFilePath;
    private String currentJavaVersion;
    private boolean fileChanged;
    private boolean needToChangeFile;

    public boolean isProjectJenvExists() {
        return projectJenvExists;
    }

    public void setProjectJenvExists(boolean projectJenvExists) {
        this.projectJenvExists = projectJenvExists;
    }

    public String getProjectJenvFilePath() {
        return projectJenvFilePath;
    }

    public void setProjectJenvFilePath(String projectJenvFilePath) {
        this.projectJenvFilePath = projectJenvFilePath;
    }

    @Deprecated
    public String getCurrentJavaVersion() {
        return currentJavaVersion;
    }

    @Deprecated
    public void setCurrentJavaVersion(String currentJavaVersion) {
        this.currentJavaVersion = currentJavaVersion;
    }

    public boolean isFileChanged() {
        return fileChanged;
    }

    public void setFileChanged(boolean fileChanged) {
        this.fileChanged = fileChanged;
    }

    public boolean isNeedToChangeFile() {
        return needToChangeFile;
    }

    public void setNeedToChangeFile(boolean needToChangeFile) {
        this.needToChangeFile = needToChangeFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JenvState that = (JenvState) o;
        return new EqualsBuilder()
                .append(projectJenvExists, that.projectJenvExists)
                .append(projectJenvFilePath, that.projectJenvFilePath)
                .append(currentJavaVersion, that.currentJavaVersion)
                .append(fileChanged, that.fileChanged)
                .append(needToChangeFile, that.needToChangeFile)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(projectJenvExists)
                .append(projectJenvFilePath)
                .append(currentJavaVersion)
                .append(fileChanged)
                .append(needToChangeFile)
                .toHashCode();
    }
}
