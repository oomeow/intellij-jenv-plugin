package com.example.jenv.service;

import com.example.jenv.constant.JenvJdkExistsType;
import com.example.jenv.model.JenvSdkModel;
import com.example.jenv.util.JenvUtils;
import com.example.jenv.util.JenvVersionParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JenvJdkTableService {
    private final List<JenvSdkModel> myJenvSdks = new ArrayList<>();

    public static JenvJdkTableService getInstance() {
        return ApplicationManager.getApplication().getService(JenvJdkTableService.class);
    }

    public @NotNull List<JenvSdkModel> getAllJdks() {
        return myJenvSdks;
    }

    public @NotNull List<JenvSdkModel> getAllJenvJdks() {
        List<JenvSdkModel> jenvJdks = new ArrayList<>();
        for (JenvSdkModel myJenvSdk : myJenvSdks) {
            if (myJenvSdk.getExistsType().equals(JenvJdkExistsType.Jenv)) {
                jenvJdks.add(myJenvSdk);
            }
        }
        return jenvJdks;
    }

    public @NotNull List<JenvSdkModel> getJenvJdksInIdea() {
        List<JenvSdkModel> jenvJdks = new ArrayList<>();
        for (JenvSdkModel myJenvSdk : myJenvSdks) {
            if (myJenvSdk.getExistsType().equals(JenvJdkExistsType.Both)) {
                jenvJdks.add(myJenvSdk);
            }
        }
        return jenvJdks;
    }

    public @NotNull List<JenvSdkModel> getNotJenvJdksInIdea() {
        List<JenvSdkModel> otherJdks = new ArrayList<>();
        for (JenvSdkModel myJenvSdk : myJenvSdks) {
            if (myJenvSdk.getExistsType().equals(JenvJdkExistsType.Idea)) {
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
        List<JenvSdkModel> ideaSdkCheckJenvList = new ArrayList<>();
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
                        } else {
                            jenvJdkExistsType = JenvJdkExistsType.OnlyNameNotMatch;
                        }
                        break;
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
        System.out.println("myJenvSdks = " + myJenvSdks);
    }

}
