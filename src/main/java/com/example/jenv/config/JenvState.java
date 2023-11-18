package com.example.jenv.config;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class JenvState {
    private boolean projectJenvExists;
    private String projectJenvFilePath;
    private String currentJavaVersion;
    private boolean fileHasChange;

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

    public String getCurrentJavaVersion() {
        return currentJavaVersion;
    }

    public void setCurrentJavaVersion(String currentJavaVersion) {
        this.currentJavaVersion = currentJavaVersion;
    }

    public boolean isFileHasChange() {
        return fileHasChange;
    }

    public void setFileHasChange(boolean fileHasChange) {
        this.fileHasChange = fileHasChange;
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
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(projectJenvExists)
                .append(projectJenvFilePath)
                .append(currentJavaVersion)
                .toHashCode();
    }
}
