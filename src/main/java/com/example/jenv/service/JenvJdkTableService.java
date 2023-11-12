package com.example.jenv.service;

import com.example.jenv.constant.JenvJdkExistsType;
import com.example.jenv.model.JenvSdkModel;
import com.example.jenv.util.JenvUtils;
import com.example.jenv.util.JenvVersionParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class JenvJdkTableService {
    private final List<JenvSdkModel> myJenvSdks = new LinkedList<>();

    public static JenvJdkTableService getInstance() {
        return ApplicationManager.getApplication().getService(JenvJdkTableService.class);
    }

    public boolean checkIsOnlyJenv(JenvSdkModel jenvSdkModel) {
        JenvJdkExistsType existsType = jenvSdkModel.getExistsType();
        return existsType.equals(JenvJdkExistsType.Jenv);
    }

    public boolean checkIsJenv(JenvSdkModel jenvSdkModel) {
        JenvJdkExistsType existsType = jenvSdkModel.getExistsType();
        return !existsType.equals(JenvJdkExistsType.Idea);
    }

    public boolean checkIsIdea(JenvSdkModel jenvSdkModel) {
        JenvJdkExistsType existsType = jenvSdkModel.getExistsType();
        return !existsType.equals(JenvJdkExistsType.Jenv);
    }

    public boolean checkIsIdeaAndIsJenv(JenvSdkModel jenvSdkModel) {
        JenvJdkExistsType existsType = jenvSdkModel.getExistsType();
        return existsType.equals(JenvJdkExistsType.OnlyHomePathMatch) || existsType.equals(JenvJdkExistsType.OnlyNameNotMatch) || existsType.equals(JenvJdkExistsType.Both);
    }

    public boolean checkIsBoth(JenvSdkModel jenvSdkModel) {
        JenvJdkExistsType existsType = jenvSdkModel.getExistsType();
        return existsType.equals(JenvJdkExistsType.Both);
    }


    public boolean checkIsIdeaAndNotJenv(JenvSdkModel jenvSdkModel) {
        JenvJdkExistsType existsType = jenvSdkModel.getExistsType();
        return existsType.equals(JenvJdkExistsType.Idea) || existsType.equals(JenvJdkExistsType.OnlyMajorVersionMatch);
    }

    public @NotNull List<JenvSdkModel> getAllIdeaAndJenvJdks() {
        return myJenvSdks;
    }

    public @NotNull List<JenvSdkModel> getAllJenvJdks() {
        List<JenvSdkModel> jenvJdks = new LinkedList<>();
        for (JenvSdkModel myJenvSdk : myJenvSdks) {
            if (checkIsOnlyJenv(myJenvSdk)) {
                jenvJdks.add(myJenvSdk);
            }
        }
        return jenvJdks;
    }

    public @NotNull List<JenvSdkModel> getJdksInIdeaAndInJenv() {
        List<JenvSdkModel> jenvJdks = new LinkedList<>();
        for (JenvSdkModel myJenvSdk : myJenvSdks) {
            if (checkIsIdeaAndIsJenv(myJenvSdk)) {
                jenvJdks.add(myJenvSdk);
            }
        }
        return jenvJdks;
    }

    public @NotNull List<JenvSdkModel> getJdksInIdeaAndNotInJenv() {
        List<JenvSdkModel> otherJdks = new LinkedList<>();
        for (JenvSdkModel myJenvSdk : myJenvSdks) {
            if (checkIsIdeaAndNotJenv(myJenvSdk)) {
                otherJdks.add(myJenvSdk);
            }
        }
        return otherJdks;
    }

    public void refreshJenvJdks() {
        myJenvSdks.clear();
        List<File> jenvJdkVersionFiles = JenvUtils.getJenvJdkVersionFiles();
        for (File jenvJdkVersionFile : jenvJdkVersionFiles) {
            JenvSdkModel jenvSdkModel = new JenvSdkModel();
            try {
                jenvSdkModel.setExistsType(JenvJdkExistsType.Jenv);
                jenvSdkModel.setName(jenvJdkVersionFile.getName());
                String version = jenvJdkVersionFile.getName();
                jenvSdkModel.setVersion(JenvVersionParser.tryParser(version));
                jenvSdkModel.setMajorVersion(JenvVersionParser.tryParserAndGetMajorVersion(version));
                jenvSdkModel.setHomePath(jenvJdkVersionFile.getPath());
                jenvSdkModel.setCanonicalPath(jenvJdkVersionFile.getCanonicalPath());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            myJenvSdks.add(jenvSdkModel);
        }
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        List<JenvSdkModel> ideaSdkCheckJenvList = new LinkedList<>();
        for (Sdk jdk : allJdks) {
            if (!(jdk.getSdkType() instanceof JavaSdkType)) {
                continue;
            }
            JenvSdkModel jenvSdkModel = new JenvSdkModel();
            String majorVersion = JenvVersionParser.tryParserAndGetMajorVersion(jdk.getVersionString());
            JenvJdkExistsType jenvJdkExistsType = JenvJdkExistsType.Idea;
            for (JenvSdkModel myJenvSdk : myJenvSdks) {
                boolean nameMatch = false;
                boolean majorVersionMatch = false;
                boolean homePathMatch = false;
                if (myJenvSdk.getName().equals(jdk.getName())) {
                    nameMatch = true;
                }
                if (myJenvSdk.getMajorVersion().equals(majorVersion)) {
                    majorVersionMatch = true;
                }
                if (myJenvSdk.getHomePath().equals(jdk.getHomePath())) {
                    homePathMatch = true;
                }
                if (myJenvSdk.getCanonicalPath() != null && myJenvSdk.getCanonicalPath().equals(jdk.getHomePath())) {
                    homePathMatch = true;
                }
                if (majorVersionMatch) {
                    jenvJdkExistsType = JenvJdkExistsType.OnlyMajorVersionMatch;
                    if (homePathMatch) {
                        if (nameMatch) {
                            jenvJdkExistsType = JenvJdkExistsType.Both;
                            break;
                        } else {
                            jenvJdkExistsType = JenvJdkExistsType.OnlyNameNotMatch;
                        }
                    }
                } else if (homePathMatch) {
                    jenvJdkExistsType = JenvJdkExistsType.OnlyHomePathMatch;
                }
            }
            jenvSdkModel.setExistsType(jenvJdkExistsType);
            jenvSdkModel.setName(jdk.getName());
            jenvSdkModel.setVersion(JenvVersionParser.tryParser(jdk.getVersionString()));
            jenvSdkModel.setMajorVersion(majorVersion);
            jenvSdkModel.setHomePath(jdk.getHomePath());
            jenvSdkModel.setIdeaJdkInfo(jdk);
            ideaSdkCheckJenvList.add(jenvSdkModel);
        }
        myJenvSdks.addAll(ideaSdkCheckJenvList);
        myJenvSdks.sort(JenvJdkTableService::JenvSdkModelComparator);
    }

    private static int JenvSdkModelComparator(JenvSdkModel o1, JenvSdkModel o2) {
        String o1Name = o1.getName();
        String o2Name = o2.getName();
        String[] o1Split = o1Name.split("[._]");
        String[] o2Split = o2Name.split("[._]");
        boolean o1Digit = Character.isDigit(o1Name.charAt(0));
        boolean o2Digit = Character.isDigit(o2Name.charAt(0));
        if (o1Digit) {
            if (!o2Digit) {
                return -1;
            } else {
                int o1Length = o1Split.length;
                int o2Length = o2Split.length;
                int o1First = Integer.parseInt(o1Split[0]);
                int o2First = Integer.parseInt(o2Split[0]);
                if (o1First == o2First) {
                    if (o1Length >= 2 && o2Length >= 2) {
                        int o1Second = Integer.parseInt(o1Split[1]);
                        int o2Second = Integer.parseInt(o2Split[1]);
                        if (o1Second == o2Second) {
                            if (o1Length >= 3 && o2Length >= 3) {
                                int o1Third = Integer.parseInt(o1Split[2]);
                                int o2Third = Integer.parseInt(o2Split[2]);
                                return Integer.compare(o1Third, o2Third);
                            }
                            if (o1Length > o2Length) {
                                return 1;
                            } else {
                                return -1;
                            }
                        }
                        return Integer.compare(o1Second, o2Second);
                    }
                    if (o1Length > o2Length) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                return Integer.compare(o1First, o2First);
            }
        } else {
            if (o2Digit) {
                return 1;
            } else {
                char o1Char = o1Name.charAt(0);
                char o2Char = o2Name.charAt(0);
                if (o1Char == o2Char) {
                    if (o1.getMajorVersion().equals(o2.getMajorVersion())) {
                        if (o1Name.length() > o2Name.length()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                    int thisMajor = Integer.parseInt(o1.getMajorVersion());
                    int targetMajor = Integer.parseInt(o2.getMajorVersion());
                    if (thisMajor > targetMajor) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    if (o1Char > o2Char) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        }
    }

}
