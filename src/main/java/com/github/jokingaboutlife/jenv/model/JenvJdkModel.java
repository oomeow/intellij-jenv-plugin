package com.github.jokingaboutlife.jenv.model;

import com.github.jokingaboutlife.jenv.constant.JdkExistsType;
import com.github.jokingaboutlife.jenv.util.JenvVersionParser;
import com.intellij.openapi.projectRoots.Sdk;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class JenvJdkModel implements Comparable<JenvJdkModel> {
    private String name;
    private String version;
    private String shortVersion;
    private String majorVersion;
    private String homePath;
    private String canonicalPath;
    // exists in IDEA jdk need
    private JdkExistsType existsType;
    private Sdk ideaJdkInfo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        this.shortVersion = JenvVersionParser.tryParseAndGetShortVersion(version);
        this.majorVersion = JenvVersionParser.tryParseAndGetMajorVersion(version);
    }

    public String getShortVersion() {
        return shortVersion;
    }

    @Deprecated
    public void setShortVersion(String shortVersion) {
        this.shortVersion = shortVersion;
    }

    public String getMajorVersion() {
        return majorVersion;
    }

    @Deprecated
    public void setMajorVersion(String majorVersion) {
        this.majorVersion = majorVersion;
    }

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }

    public void setCanonicalPath(String canonicalPath) {
        this.canonicalPath = canonicalPath;
    }

    public JdkExistsType getExistsType() {
        return existsType;
    }

    public void setExistsType(JdkExistsType existsType) {
        this.existsType = existsType;
    }

    public Sdk getIdeaJdkInfo() {
        return ideaJdkInfo;
    }

    public void setIdeaJdkInfo(Sdk ideaJdkInfo) {
        this.ideaJdkInfo = ideaJdkInfo;
    }

    @Override
    public int compareTo(@NotNull JenvJdkModel o) {
        return StringUtils.compare(this.name, o.getName());
    }
}
