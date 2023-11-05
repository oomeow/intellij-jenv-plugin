package com.example.jenv.config;

import com.example.jenv.JenvHelper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ProjectJenvState {
    private boolean jenvInstalled;
    private boolean javaInstalled;
    private boolean projectJenvExists;
    private boolean projectOpened;
    private String projectJenvFilePath;
    private String currentJavaVersion;
    private boolean changeJdkByDialog;
    private boolean showNotJenvJdkNotification;
    private boolean jenvJdkSelected;

    public boolean isJenvInstalled() {
        return jenvInstalled;
    }

    public void setJenvInstalled(boolean jenvInstalled) {
        this.jenvInstalled = jenvInstalled;
    }

    public boolean isJavaInstalled() {
        return javaInstalled;
    }

    public void setJavaInstalled(boolean javaInstalled) {
        this.javaInstalled = javaInstalled;
    }

    public boolean isProjectJenvExists() {
        return projectJenvExists;
    }

    public void setProjectJenvExists(boolean projectJenvExists) {
        this.projectJenvExists = projectJenvExists;
    }

    public boolean isProjectOpened() {
        return projectOpened;
    }

    public void setProjectOpened(boolean projectOpened) {
        this.projectOpened = projectOpened;
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

    public boolean isChangeJdkByDialog() {
        return changeJdkByDialog;
    }

    public void setChangeJdkByDialog(boolean changeJdkByDialog) {
        this.changeJdkByDialog = changeJdkByDialog;
    }

    public boolean isShowNotJenvJdkNotification() {
        return showNotJenvJdkNotification;
    }

    public void setShowNotJenvJdkNotification(boolean showNotJenvJdkNotification) {
        this.showNotJenvJdkNotification = showNotJenvJdkNotification;
    }

    public boolean isJenvJdkSelected() {
        return jenvJdkSelected;
    }

    public void setJenvJdkSelected(boolean jenvJdkSelected) {
        this.jenvJdkSelected = jenvJdkSelected;
    }

    public String getFormattedJavaVersion() {
        if (jenvJdkSelected) {
            return JenvHelper.formatJdkVersion(currentJavaVersion);
        }
        return currentJavaVersion;
    }

    public String getJenvJavaVersion() {
        double parsed = Double.parseDouble(currentJavaVersion);
        if (parsed >= 10.0) {
            parsed += 0.0;
        }
        return Double.toString(parsed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProjectJenvState that = (ProjectJenvState) o;
        return new EqualsBuilder()
                .append(jenvInstalled, that.jenvInstalled)
                .append(javaInstalled, that.javaInstalled)
                .append(projectJenvExists, that.projectJenvExists)
                .append(projectOpened, that.projectOpened)
                .append(projectJenvFilePath, that.projectJenvFilePath)
                .append(currentJavaVersion, that.currentJavaVersion)
                .append(changeJdkByDialog, that.changeJdkByDialog)
                .append(showNotJenvJdkNotification, that.showNotJenvJdkNotification)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(jenvInstalled)
                .append(javaInstalled)
                .append(projectJenvExists)
                .append(projectOpened)
                .append(projectJenvFilePath)
                .append(currentJavaVersion)
                .append(changeJdkByDialog)
                .append(showNotJenvJdkNotification)
                .toHashCode();
    }
}
