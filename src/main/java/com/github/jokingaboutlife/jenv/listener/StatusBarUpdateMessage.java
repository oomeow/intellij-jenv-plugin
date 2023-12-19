package com.github.jokingaboutlife.jenv.listener;

import com.intellij.util.messages.Topic;

public interface StatusBarUpdateMessage {

    Topic<StatusBarUpdateMessage> TOPIC = Topic.create("jEnv Status Bar Update", StatusBarUpdateMessage.class);

    void updateStatusBar();
}
