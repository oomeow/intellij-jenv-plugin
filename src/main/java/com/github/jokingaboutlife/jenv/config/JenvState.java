package com.github.jokingaboutlife.jenv.config;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class JenvState {
    private boolean localJenvFileExists;
    private String localJenvFilePath;
    private boolean needToChangeFile;

    public boolean isLocalJenvFileExists() {
        return localJenvFileExists;
    }

    public void setLocalJenvFileExists(boolean localJenvFileExists) {
        this.localJenvFileExists = localJenvFileExists;
    }

    public String getLocalJenvFilePath() {
        return localJenvFilePath;
    }

    public void setLocalJenvFilePath(String localJenvFilePath) {
        this.localJenvFilePath = localJenvFilePath;
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
                .append(localJenvFileExists, that.localJenvFileExists)
                .append(localJenvFilePath, that.localJenvFilePath)
                .append(needToChangeFile, that.needToChangeFile)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(localJenvFileExists)
                .append(localJenvFilePath)
                .append(needToChangeFile)
                .toHashCode();
    }
}
