package com.example.jenv.constant;

public enum DialogMessage {
    SELECT_JDK_VERSION("Select JDK Version", ""),
    JENV_NOT_INSTALL("You have to install Jenv", "<html>Can't find Jenv. <br/> If you click OK button, go to Jenv installation guide.</html>"),
    JAVA_NOT_INSTALL("You have to install Java", "Please install java in your path."),
    PROJECT_JAVA_VERSION_NOT_FOUND("'.java-version' file not found", "Create a '.java-version' file or type jenv local command."),
    JDK_NOT_FOUND("JDK not found", "<html>Java version (%s) not found in Idea <br/> Please check JDK is exists and then open Project Structure and then add JDK.</html>")
    ;

    String title;
    String description;

    DialogMessage(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
