package com.example.jenv.constant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JenvConstants {
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String JENV_DIR = USER_HOME + File.separator + ".jenv";
    public static final String JENV_VERSIONS_DIR = JENV_DIR + File.separator + "versions";
    public static final String VERSION_FILE = ".java-version";
    public static final String JENV_INSTALL_URL = "https://www.jenv.be";
    public static final String NOTIFICATION_GROUP_ID = "Jenv Error";
    public static final Map<String, List<String>> JENV_JDK_MAP = new HashMap<>();
    public static final List<String> JENV_JDK_HOME_PATH_LIST = new ArrayList<>();
}