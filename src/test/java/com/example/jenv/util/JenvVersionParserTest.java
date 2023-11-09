package com.example.jenv.util;

import junit.framework.TestCase;

public class JenvVersionParserTest extends TestCase {

    public void testName() {
        String versionString = "zulu64-1.8.0.382";
        String javaVersion = JenvVersionParser.tryParser(versionString);
        System.out.println("javaVersion = " + javaVersion);
        String majorVersion = JenvVersionParser.tryParserAndGetMajorVersion(versionString);
        System.out.println("majorVersion = " + majorVersion);
    }

}