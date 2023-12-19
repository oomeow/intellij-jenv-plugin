package com.github.jokingaboutlife.jenv.config;

public class JenvState {
    private boolean localJenvFileExists;
    private String localJenvFilePath;
    private boolean needToChangeFile;
    private long jenvFileModificationStamp;

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

    public long getJenvFileModificationStamp() {
        return jenvFileModificationStamp;
    }

    public void setJenvFileModificationStamp(long jenvFileModificationStamp) {
        this.jenvFileModificationStamp = jenvFileModificationStamp;
    }
}
