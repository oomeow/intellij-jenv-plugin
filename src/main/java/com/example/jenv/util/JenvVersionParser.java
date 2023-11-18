package com.example.jenv.util;

import com.intellij.util.lang.JavaVersion;

public class JenvVersionParser {
    private static final String[] providers = new String[]{"oracle", "zulu", "zulu_prime", "graalvm", "corretto", "sap",
            "temurin", "jetbrains", "kona", "openlogic", "semeru", "semeru_certified", "dragonwell", "ibm", "openjdk", "other"};

    public static String tryParser(String jdkVersion) {
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

    public static String tryParserAndGetMajorVersion(String jdkVersion) {
        String jdkVersionInfo = tryParser(jdkVersion);
        try {
            String[] parts = jdkVersionInfo.split("[._]");
            int firstVer = Integer.parseInt(parts[0]);
            int majorVersion = firstVer == 1 && parts.length > 1 ? Integer.parseInt(parts[1]) : firstVer;
            return String.valueOf(majorVersion);
        } catch (NumberFormatException e) {
            return "";
        }
    }
}
