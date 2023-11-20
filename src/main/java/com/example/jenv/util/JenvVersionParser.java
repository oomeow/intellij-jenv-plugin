package com.example.jenv.util;

import com.intellij.util.lang.JavaVersion;

public class JenvVersionParser {
    private static final String[] providers = new String[]{"oracle", "zulu", "zulu_prime", "graalvm", "corretto", "sap",
            "temurin", "jetbrains", "kona", "openlogic", "semeru", "semeru_certified", "dragonwell", "ibm", "openjdk", "other"};

    public static String tryParse(String jdkVersion) {
        String resultVersion = null;
        for (String provider : providers) {
            if (jdkVersion.contains(provider)) {
                int index = jdkVersion.indexOf("-") + 1;
                jdkVersion = jdkVersion.substring(index);
                break;
            }
        }
        try {
            JavaVersion javaVersion = JavaVersion.tryParse(jdkVersion);
            resultVersion = javaVersion != null ? javaVersion.toString() : null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return resultVersion;
    }

    public static String tryParseAndGetShortVersion(String jdkVersion) {
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
