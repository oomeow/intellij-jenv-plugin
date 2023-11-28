package com.github.jokingaboutlife.jenv.constant;

import org.jetbrains.annotations.NonNls;

import java.io.File;

public class JenvConstants {
    public static final @NonNls String BUNDLE = "messages.JenvBundle";
    public static final String USER_HOME;
    public static final String JENV_DIR;
    public static final String JENV_VERSIONS_DIR;
    public static final String VERSION_FILE = ".java-version";

    static {
        USER_HOME = System.getProperty("user.home");
        JENV_DIR = USER_HOME + File.separator + ".jenv";
        JENV_VERSIONS_DIR = JENV_DIR + File.separator + "versions";
    }
}