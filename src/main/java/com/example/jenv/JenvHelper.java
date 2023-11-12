package com.example.jenv;

import com.example.jenv.constant.JenvConstants;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JenvHelper {

    public static List<Sdk> getAllIdeaJdks() {
        List<Sdk> jdks = new ArrayList<>();
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        Collections.addAll(jdks, allJdks);
        return jdks;
    }

    public static List<String> getAllIdeaJdkVersionList() {
        return getAllIdeaJdks().stream()
                .map(Sdk::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getIdeaJdkVersionList(boolean isJenvJdk) {
        Predicate<Sdk> sdkPredicate = o -> JenvConstants.JENV_JDK_HOME_PATH_LIST.contains(o.getHomePath());
        if (!isJenvJdk) {
            sdkPredicate = o -> !JenvConstants.JENV_JDK_HOME_PATH_LIST.contains(o.getHomePath());
        }
        return getAllIdeaJdks().stream()
                .filter(sdkPredicate)
                .map(Sdk::getName)
                .collect(Collectors.toList());
    }

    public static void refreshAllJenvJdkInfo() {
        JenvConstants.JENV_JDK_MAP.clear();
        JenvConstants.JENV_JDK_HOME_PATH_LIST.clear();
        File jenvVersionsDir = new File(JenvConstants.JENV_VERSIONS_DIR);
        if (jenvVersionsDir.exists()) {
            File[] children = jenvVersionsDir.listFiles();
            if (children == null) {
                return;
            }
            try {
                for (File jdkVersionDir : children) {
                    List<String> jenvHomePathList = new ArrayList<>();
                    jenvHomePathList.add(jdkVersionDir.getPath());
                    jenvHomePathList.add(jdkVersionDir.getCanonicalPath());
                    JenvConstants.JENV_JDK_MAP.put(jdkVersionDir.getName(), jenvHomePathList);
                    if (!JenvConstants.JENV_JDK_HOME_PATH_LIST.contains(jdkVersionDir.getPath())) {
                        JenvConstants.JENV_JDK_HOME_PATH_LIST.add(jdkVersionDir.getPath());
                    }
                    if (!JenvConstants.JENV_JDK_HOME_PATH_LIST.contains(jdkVersionDir.getCanonicalPath())) {
                        JenvConstants.JENV_JDK_HOME_PATH_LIST.add(jdkVersionDir.getCanonicalPath());
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static int getCurrentJdkVersionPosition(String currentVersion, boolean isJenv) {
        List<String> versionList = getIdeaJdkVersionList(isJenv);
        int index = 0;
        for (int i = 0; i < versionList.size(); i++) {
            if (versionList.get(i).equals(currentVersion)) {
                index = i;
            }
        }
        return index;
    }

    public static boolean checkIdeaJdkExistsByVersion(String jdkVersion) {
        if (StringUtils.isBlank(jdkVersion)) {
            return false;
        }
        List<String> allIdeaJdksVersionList = getAllIdeaJdkVersionList();
        for (String ideaJdkVersion : allIdeaJdksVersionList) {
            if (jdkVersion.equals(ideaJdkVersion)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkJenvJdkExistsByJdkHomePath(String jdkVersion) {
        List<String> currentJenvVersionPathList = JenvConstants.JENV_JDK_MAP.get(jdkVersion);
        if (currentJenvVersionPathList != null) {
            List<Sdk> allIdeaJdks = getAllIdeaJdks();
            for (Sdk ideaJdk : allIdeaJdks) {
                if (currentJenvVersionPathList.contains(ideaJdk.getHomePath())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkIsJenvJdk(String jdkVersion) {
        List<Sdk> allIdeaJdks = getAllIdeaJdks();
        for (Sdk ideaJdk : allIdeaJdks) {
            if (ideaJdk.getName().equals(jdkVersion)) {
                List<String> currentVersionPath = JenvConstants.JENV_JDK_MAP.get(jdkVersion);
                if (currentVersionPath != null) {
                    return currentVersionPath.contains(ideaJdk.getHomePath());
                }
                return false;
            }
        }
        return false;
    }

//    public static Notification createWarnNotification(NotifyMessage message) {
//        return createNotification(message, NotificationType.INFORMATION);
//    }
//
//    public static Notification createErrorNotification(NotifyMessage message) {
//        return createNotification(message, NotificationType.INFORMATION);
//    }
//
//    public static Notification createNotification(NotifyMessage message, NotificationType type) {
//        return new Notification(JenvConstants.NOTIFICATION_GROUP_ID, message.getTitle(), message.getContent(), type);
//    }

}
