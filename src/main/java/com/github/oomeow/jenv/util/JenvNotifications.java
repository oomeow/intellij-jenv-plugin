package com.github.oomeow.jenv.util;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class JenvNotifications {

    private static final String NOTIFICATION_GROUP_ID = "jEnv";
    private static final String DONT_SHOW_AGAIN_KEY_PREFIX = "notification.jEnv.dont.show.again";

    private static void showNotificationOrDontShowAgain(String title, String content, Project project, NotificationType type, boolean showAgainBtn) {
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification(title, content, type);
        if (showAgainBtn) {
            notification.addAction(new DontShowAgainAction(title));
        }
        if (!isDoNotShowAgain(title)) {
            notification.notify(project);
        }
    }

    public static void showInfoNotification(String title, String content, Project project, boolean showAgainBtn) {
        showNotificationOrDontShowAgain(title, content, project, NotificationType.INFORMATION, showAgainBtn);
    }

    public static void showWarnNotification(String title, String content, Project project, boolean showAgainBtn) {
        showNotificationOrDontShowAgain(title, content, project, NotificationType.WARNING, showAgainBtn);
    }

    public static void showErrorNotification(String title, String content, Project project, boolean showAgainBtn) {
        showNotificationOrDontShowAgain(title, content, project, NotificationType.ERROR, showAgainBtn);
    }

    public static void setDontShowAgain(String key, boolean show) {
        String fullKey = DONT_SHOW_AGAIN_KEY_PREFIX + "." + key;
        PropertiesComponent.getInstance().setValue(fullKey, show);
    }

    public static boolean isDoNotShowAgain(String key) {
        String fullKey = DONT_SHOW_AGAIN_KEY_PREFIX + "." + key;
        return PropertiesComponent.getInstance().getBoolean(fullKey, false);
    }

    static class DontShowAgainAction extends NotificationAction {
        private final String key;

        public DontShowAgainAction(String key) {
            super("Don't show again");
            this.key = key;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
            notification.expire();
            setDontShowAgain(key, true);
        }
    }
}
