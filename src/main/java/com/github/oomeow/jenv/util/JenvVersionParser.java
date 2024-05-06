package com.github.oomeow.jenv.util;

import com.intellij.util.lang.JavaVersion;

public class JenvVersionParser {
    private static final String[] providers = new String[]{"oracle", "zulu", "zulu_prime", "graalvm", "corretto", "sap",
            "temurin", "jetbrains", "kona", "openlogic", "semeru", "semeru_certified", "dragonwell", "ibm", "openjdk", "other"};

    public static String tryParse(String jdkVersion) {
        if (jdkVersion == null) {
            return null;
        }
        String resultVersion;
        for (String provider : providers) {
            if (jdkVersion.contains(provider)) {
                int index = jdkVersion.indexOf("-") + 1;
                jdkVersion = jdkVersion.substring(index);
                break;
            }
        }
        JavaVersion javaVersion = JavaVersion.tryParse(jdkVersion);
        resultVersion = javaVersion != null ? javaVersion.toString() : null;
        return resultVersion;
    }

    public static String tryParseAndGetShortVersion(String jdkVersion) {
        if (jdkVersion == null) {
            return null;
        }
        String shortVersion;
        String javaVersion = tryParse(jdkVersion);
        String[] split = javaVersion.split("[._]");
        if (split.length > 1) {
            if (Integer.parseInt(split[1]) > 0) {
                int index = javaVersion.indexOf(split[1]) + 1;
                shortVersion = javaVersion.substring(0, index);
            } else {
                shortVersion = split[0];
            }
        } else {
            shortVersion = javaVersion;
        }
        return shortVersion;
    }

    public static String tryParseAndGetMajorVersion(String jdkVersion) {
        if (jdkVersion == null) {
            return null;
        }
        String majorVersion;
        String jdkVersionInfo = tryParse(jdkVersion);
        try {
            String[] parts = jdkVersionInfo.split("[._]");
            int firstVer = Integer.parseInt(parts[0]);
            int majorVersionInt = firstVer == 1 && parts.length > 1 ? Integer.parseInt(parts[1]) : firstVer;
            majorVersion = String.valueOf(majorVersionInt);
        } catch (NumberFormatException e) {
            majorVersion = null;
        }
        return majorVersion;
    }
}
