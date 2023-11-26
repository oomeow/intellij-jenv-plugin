package com.github.jokingaboutlife.jenv.util;

import junit.framework.TestCase;

public class JenvVersionParserTest extends TestCase {

    public void testName() {
        String versionString = "zulu64-1.8.0.382";
//        String versionString = "Azul Zulu version 1.8.0_382";
        String javaVersion = JenvVersionParser.tryParse(versionString);
        System.out.println("javaVersion = " + javaVersion);
        String majorVersion = JenvVersionParser.tryParseAndGetMajorVersion(versionString);
        System.out.println("majorVersion = " + majorVersion);
    }

}