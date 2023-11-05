package com.example.jenv;

import com.example.jenv.constant.JenvConstants;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JenvHelper {

    public static boolean isWindows() {
        return StringUtils.contains(System.getProperty("os.name").toLowerCase(), "win");
    }

    public static List<String> getAllIdeaJdkVersionList() {
        return Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                .map(Sdk::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getJenvIdeaJdkVersionList() {
        return Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                .filter(o -> JenvConstants.JENV_JDK_HOME_PATH_LIST.contains(o.getHomePath()))
                .map(Sdk::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getNotJenvIdeaJdkVersionList() {
        return Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                .filter(o -> !JenvConstants.JENV_JDK_HOME_PATH_LIST.contains(o.getHomePath()))
                .map(Sdk::getName)
                .collect(Collectors.toList());
    }

    public static void findAllJenvJdkHomePath() {
        JenvConstants.JENV_JDK_HOME_PATH_LIST.clear();
        String userHomePath = System.getProperty("user.home");
        String jEnvVersionPath = userHomePath + File.separator + JenvConstants.JENV_VERSIONS_DIR;
        VirtualFile jenvVersionDir = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jEnvVersionPath));
        if (jenvVersionDir != null && jenvVersionDir.exists()) {
            VirtualFile[] children = jenvVersionDir.getChildren();
            for (VirtualFile jdkVersionDir : children) {
                if (!JenvConstants.JENV_JDK_HOME_PATH_LIST.contains(jdkVersionDir.getPath())) {
                    JenvConstants.JENV_JDK_HOME_PATH_LIST.add(jdkVersionDir.getPath());
                }
                if (!JenvConstants.JENV_JDK_HOME_PATH_LIST.contains(jdkVersionDir.getCanonicalPath())) {
                    JenvConstants.JENV_JDK_HOME_PATH_LIST.add(jdkVersionDir.getCanonicalPath());
                }
            }
        }
    }

    public static Integer getCurrentJdkVersionPosition(String currentVersion, boolean isJenv, boolean isOther) {
        List<String> versionList = getAllIdeaJdkVersionList();
        if (isJenv) {
            versionList = getJenvIdeaJdkVersionList();
        } else if (isOther) {
            versionList = getNotJenvIdeaJdkVersionList();
        }
        Integer index = null;
        for (int i = 0; i < versionList.size(); i++) {
            if (versionList.get(i).equals(currentVersion)) {
                index = i;
            }
        }
        return index;
    }

    public static String formatJdkVersion(String jdkVersion) {
        String result = jdkVersion.trim();
        if (StringUtils.isBlank(jdkVersion)) {
            result = StringUtils.EMPTY;
        }
        double parsed = Double.parseDouble(jdkVersion);
        if (parsed >= 10.0) {
            DecimalFormat format = new DecimalFormat();
            format.setDecimalSeparatorAlwaysShown(false);
            result = format.format(parsed);
        }
        return result;
    }

    public static boolean checkIdeaJDKExists(String jdkVersion) {
        List<String> allIdeaJdksVersionList = getAllIdeaJdkVersionList();
        for (String ideaJdkVersion : allIdeaJdksVersionList) {
            if (jdkVersion.equals(ideaJdkVersion)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkIsJenvJdk(String jdkVersion) {
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        for (Sdk jdk : allJdks) {
            if (jdk.getName().equals(jdkVersion) && JenvConstants.JENV_JDK_HOME_PATH_LIST.contains(jdk.getHomePath())) {
                return true;
            }
        }
        return false;
    }
}
