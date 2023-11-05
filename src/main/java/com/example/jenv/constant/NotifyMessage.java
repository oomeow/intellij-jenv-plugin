package com.example.jenv.constant;

public enum NotifyMessage {
    NOT_JENV_JDK("JDK not belong to Jenv", "<html>Java version (%s) found in Idea, but not belong to Jenv <br/> The JDK of project will be change, event it not belong to Jenv. if you want to use JDK of Jenv, you need to change the path of the JDK.</html>"),
    NOT_FOUND_JDK("JDK not found", "<html>Java version (%s) not found in Idea. <br/> Please check JDK is exists and then open Project Structure to add JDK.</html>"),
    ;

    private final String title;
    private String content;

    NotifyMessage(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
