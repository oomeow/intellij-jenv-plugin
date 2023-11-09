package com.example.jenv.constant;

public enum NotifyMessage {
    NOT_JENV_JDK(
            "JDK found but Not Jenv JDK",
            "<html>Java version (%s) all exist in Idea and Jenv, but the jdk home path is not the path of Jenv, if you need, you can change the home path.</html>",
            ""
    ),
    NOT_FOUND_JDK(
            "JDK not found",
            "<html>Java version (%s) not found in Idea.<br/>Please check Jdk is exists and then open project structure to add Jdk.</html>",
            ""
    ),
    ;

    private final String title;
    private final String formatTemplate;
    private String content;

    NotifyMessage(String title, String formatTemplate, String content) {
        this.title = title;
        this.formatTemplate = formatTemplate;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getFormatTemplate() {
        return formatTemplate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
