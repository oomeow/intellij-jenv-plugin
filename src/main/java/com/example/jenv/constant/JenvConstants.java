package com.example.jenv.constant;

import com.intellij.openapi.projectRoots.SdkType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class JenvConstants {
    public static final @NonNls String BUNDLE = "messages.JenvBundle";
    public static final String USER_HOME;
    public static final String JENV_DIR;
    public static final String JENV_VERSIONS_DIR;
    public static final String VERSION_FILE = ".java-version";
    public static final String JENV_INSTALL_URL = "https://www.jenv.be";
    public static final @NotNull SdkType PROJECT_JENV_JDK_TYPE;
    public static final String JDK_RENAME_SUFFIX = "-renamed";

    static {
        USER_HOME = System.getProperty("user.home");
        JENV_DIR = USER_HOME + File.separator + ".jenv";
        JENV_VERSIONS_DIR = JENV_DIR + File.separator + "versions";
        PROJECT_JENV_JDK_TYPE = Objects.requireNonNull(SdkType.findByName("JavaSDK"));
    }
}