package com.github.oomeow.jenv.util;

import com.github.oomeow.jenv.constant.JdkExistsType;
import com.github.oomeow.jenv.constant.JenvConstants;
import com.github.oomeow.jenv.model.JenvJdkModel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JenvUtils {
    public static boolean checkJenvInstalled() {
        File file = new File(JenvConstants.JENV_DIR);
        return file.exists() && file.isDirectory();
    }

    public static @NotNull List<File> getJenvJdkVersionFiles() {
        List<File> files = new ArrayList<>();
        File file = new File(JenvConstants.JENV_VERSIONS_DIR);
        if (file.exists() && file.isDirectory()) {
            File[] jenvJdkVersions = file.listFiles();
            if (jenvJdkVersions != null && jenvJdkVersions.length > 0) {
                files.addAll(Arrays.asList(jenvJdkVersions));
            }
        }
        return files;
    }

    public static boolean checkIsIdeaAndIsJenv(@NotNull JenvJdkModel jenvJdkModel) {
        JdkExistsType existsType = jenvJdkModel.getExistsType();
        return existsType.equals(JdkExistsType.OnlyNameNotMatch) || existsType.equals(JdkExistsType.Both);
    }

    public static boolean checkIsBoth(@NotNull JenvJdkModel jenvJdkModel) {
        JdkExistsType existsType = jenvJdkModel.getExistsType();
        return existsType.equals(JdkExistsType.Both);
    }

    public static boolean checkIsIdeaAndNotJenv(@NotNull JenvJdkModel jenvJdkModel) {
        JdkExistsType existsType = jenvJdkModel.getExistsType();
        return existsType.equals(JdkExistsType.OnlyInIDEA) || existsType.equals(JdkExistsType.OnlyMajorVersionMatch);
    }

}
