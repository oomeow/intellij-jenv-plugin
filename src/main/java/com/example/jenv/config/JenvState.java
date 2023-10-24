package com.example.jenv.config;

import com.example.jenv.JenvHelper;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Transient;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class JenvState {
    @Transient
    private Project project;
    private String projectJenvFilePath;
    private String currentJavaVersion;
    private boolean jenvInstalled;
    private boolean javaInstalled;
    private boolean projectJenvExists;
    private boolean changeJenvByDialog;
    private boolean projectOpened;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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

    public String getFormattedJavaVersion() {
        return JenvHelper.formatJdkVersion(currentJavaVersion);
    }

    public String getJenvJavaVersion() {
        double parsed = Double.parseDouble(currentJavaVersion);
        if (parsed >= 10.0) {
            parsed += 0.0;
        }
        return Double.toString(parsed);
    }

    public boolean isChangeJenvByDialog() {
        return changeJenvByDialog;
    }

    public void setChangeJenvByDialog(boolean changeJenvByDialog) {
        this.changeJenvByDialog = changeJenvByDialog;
    }

    public boolean isProjectOpened() {
        return projectOpened;
    }

    public void setProjectOpened(boolean projectOpened) {
        this.projectOpened = projectOpened;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        JenvState that = (JenvState) o;

        return new EqualsBuilder()
                .append(projectJenvFilePath, that.projectJenvFilePath)
                .append(currentJavaVersion, that.currentJavaVersion)
                .append(jenvInstalled, that.jenvInstalled)
                .append(changeJenvByDialog, that.changeJenvByDialog)
                .append(projectOpened, that.projectOpened)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(projectJenvFilePath)
                .append(currentJavaVersion)
                .append(jenvInstalled)
                .append(changeJenvByDialog)
                .append(projectOpened)
                .toHashCode();
    }
}
