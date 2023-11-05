package com.example.jenv.constant;

public enum DialogMessage {
    SELECT_JDK_VERSION("Select JDK Version", ""),
    JENV_NOT_INSTALL("You have to install Jenv", "<html>Can't find Jenv. <br/> If you click OK button, go to Jenv installation guide.</html>"),
    JAVA_NOT_INSTALL("You have to install Java", "Please install java in your path."),
    PROJECT_JAVA_VERSION_NOT_FOUND("'.java-version' file not found", "Create a '.java-version' file or type jenv local command.");

    final String title;
    final String description;

    DialogMessage(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}
