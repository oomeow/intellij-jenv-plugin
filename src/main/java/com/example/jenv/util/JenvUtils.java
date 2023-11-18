package com.example.jenv.util;

import com.example.jenv.constant.JenvConstants;
import com.example.jenv.constant.JenvJdkExistsType;
import com.example.jenv.model.JenvJdkModel;
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

    public static boolean checkIsJenv(JenvJdkModel jenvJdkModel) {
        JenvJdkExistsType existsType = jenvJdkModel.getExistsType();
        return existsType.equals(JenvJdkExistsType.Jenv) || existsType.equals(JenvJdkExistsType.OnlyNameNotMatch);
    }

    public static boolean checkIsIdea(JenvJdkModel jenvJdkModel) {
        JenvJdkExistsType existsType = jenvJdkModel.getExistsType();
        return !existsType.equals(JenvJdkExistsType.Jenv);
    }

    public static boolean checkIsIdeaAndIsJenv(JenvJdkModel jenvJdkModel) {
        JenvJdkExistsType existsType = jenvJdkModel.getExistsType();
        return existsType.equals(JenvJdkExistsType.OnlyNameNotMatch) || existsType.equals(JenvJdkExistsType.Both);
    }

    public static boolean checkIsBoth(JenvJdkModel jenvJdkModel) {
        JenvJdkExistsType existsType = jenvJdkModel.getExistsType();
        return existsType.equals(JenvJdkExistsType.Both);
    }

    public static boolean checkIsIdeaAndNotJenv(JenvJdkModel jenvJdkModel) {
        JenvJdkExistsType existsType = jenvJdkModel.getExistsType();
        return existsType.equals(JenvJdkExistsType.Idea) || existsType.equals(JenvJdkExistsType.OnlyMajorVersionMatch);
    }

}
