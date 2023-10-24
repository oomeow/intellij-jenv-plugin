package com.example.jenv;

import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import org.apache.commons.lang.StringUtils;

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

    public static List<String> getAllJdkVersionList() {
        return Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                .map(Sdk::getName)
                .collect(Collectors.toList());
    }

    public static Integer getCurrentVersionPosition(String currentVersion) {
        List<String> versionList = getAllJdkVersionList();
        OptionalInt index = IntStream.range(0, versionList.size())
                .filter(idx -> StringUtils.equals(versionList.get(idx), (currentVersion)))
                .findFirst();

        if (index.isPresent()) {
            return index.getAsInt();
        }
        return null;
    }

    public static String formatJdkVersion(String jdkVersion) {
        String result = null;
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
