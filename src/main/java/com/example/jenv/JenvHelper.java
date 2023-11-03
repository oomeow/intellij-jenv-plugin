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
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JenvHelper {

    public static boolean isWindows() {
        return StringUtils.contains(System.getProperty("os.name").toLowerCase(), "win");
    }

    public static List<String> getAllIdeaJdksVersionList() {
        return Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                .map(Sdk::getName)
                .collect(Collectors.toList());
    }

    public static void getAllJenvJdkHomePath() {
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

    public static List<String> getIdeaJenvJdksVersionList() {
        return Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                .filter(o -> JenvConstants.JENV_JDK_HOME_PATH_LIST.contains(o.getHomePath()))
                .map(Sdk::getName)
                .collect(Collectors.toList());
    }

    public static Integer getCurrentVersionPosition(String currentVersion) {
        List<String> versionList = getIdeaJenvJdksVersionList();
        OptionalInt index = IntStream.range(0, versionList.size())
                .filter(idx -> StringUtils.equals(versionList.get(idx), (currentVersion)))
                .findFirst();
        if (index.isPresent()) {
            return index.getAsInt();
        }
        return null;
    }

    public static String formatJdkVersion(String jdkVersion) {
        String result = jdkVersion;
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
        Integer position = getCurrentVersionPosition(formatJdkVersion(jdkVersion));
        return position != null;
    }
}
