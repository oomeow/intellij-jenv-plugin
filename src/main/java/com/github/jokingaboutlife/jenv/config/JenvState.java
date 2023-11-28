package com.github.jokingaboutlife.jenv.config;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class JenvState {
    private boolean projectJenvExists;
    private String projectJenvFilePath;
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
                .append(needToChangeFile, that.needToChangeFile)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(projectJenvExists)
                .append(projectJenvFilePath)
                .append(needToChangeFile)
                .toHashCode();
    }
}
