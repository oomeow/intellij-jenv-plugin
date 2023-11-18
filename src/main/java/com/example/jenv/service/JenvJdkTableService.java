package com.example.jenv.service;

import com.example.jenv.constant.JenvConstants;
import com.example.jenv.constant.JenvJdkExistsType;
import com.example.jenv.dialog.JdkRenameDialog;
import com.example.jenv.model.JenvJdkModel;
import com.example.jenv.util.JenvUtils;
import com.example.jenv.util.JenvVersionParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JenvJdkTableService {

    private final List<JenvJdkModel> myJenvJdks = new LinkedList<>();
    private final List<JenvJdkModel> myIdeaJdks = new LinkedList<>();

    public static JenvJdkTableService getInstance() {
        return ApplicationManager.getApplication().getService(JenvJdkTableService.class);
    }

    public @NotNull List<JenvJdkModel> getAllJenvJdks() {
        return myJenvJdks;
    }

    public @NotNull List<JenvJdkModel> getAllIdeaJdks() {
        return myIdeaJdks;
    }

    public @NotNull List<JenvJdkModel> getJdksInIdeaAndInJenv() {
        List<JenvJdkModel> jenvJdks = new LinkedList<>();
        for (JenvJdkModel myJenvJdk : myIdeaJdks) {
            if (JenvUtils.checkIsIdeaAndIsJenv(myJenvJdk)) {
                jenvJdks.add(myJenvJdk);
            }
        }
        return jenvJdks;
    }

    public @NotNull List<JenvJdkModel> getJdksInIdeaAndNotInJenv() {
        List<JenvJdkModel> otherJdks = new LinkedList<>();
        for (JenvJdkModel myJenvJdk : myIdeaJdks) {
            if (JenvUtils.checkIsIdeaAndNotJenv(myJenvJdk)) {
                otherJdks.add(myJenvJdk);
            }
        }
        return otherJdks;
    }

    public JenvJdkModel findJenvJdkByName(String jdkName, boolean jdkInIdea) {
        JenvJdkModel jenvJdkModel = null;
        List<JenvJdkModel> list = myIdeaJdks;
        if (!jdkInIdea) {
            list = myJenvJdks;
        }
        for (JenvJdkModel myIdeaJdk : list) {
            if (myIdeaJdk.getName().equals(jdkName)) {
                jenvJdkModel = myIdeaJdk;
            }
        }
        return jenvJdkModel;
    }

    @NotNull
    private JenvJdkModel createJenvJdkModel(Sdk jdk) {
        JenvJdkModel jenvJdkModel = new JenvJdkModel();
        String majorVersion = JenvVersionParser.tryParserAndGetMajorVersion(jdk.getVersionString());
        JenvJdkExistsType jenvJdkExistsType = getIdeaJdkExistsType(jdk, majorVersion);
        jenvJdkModel.setExistsType(jenvJdkExistsType);
        jenvJdkModel.setName(jdk.getName());
        jenvJdkModel.setVersion(JenvVersionParser.tryParser(jdk.getVersionString()));
        jenvJdkModel.setMajorVersion(majorVersion);
        jenvJdkModel.setHomePath(jdk.getHomePath());
        jenvJdkModel.setIdeaJdkInfo(jdk);
        return jenvJdkModel;
    }

    private JenvJdkExistsType getIdeaJdkExistsType(Sdk jdk, String majorVersion) {
        JenvJdkExistsType jenvJdkExistsType = JenvJdkExistsType.Idea;
        for (JenvJdkModel myJenvSdk : myJenvJdks) {
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
            }
        }
        return jenvJdkExistsType;
    }

    private JenvJdkExistsType getJenvJdkExistsType(JenvJdkModel jenvJdkModel) {
        JenvJdkExistsType jenvJdkExistsType = JenvJdkExistsType.Jenv;
        for (JenvJdkModel myIdeaJdk : myIdeaJdks) {
            boolean nameMatch = false;
            boolean majorVersionMatch = false;
            boolean homePathMatch = false;
            if (myIdeaJdk.getName().equals(jenvJdkModel.getName())) {
                nameMatch = true;
            }
            if (myIdeaJdk.getMajorVersion().equals(jenvJdkModel.getMajorVersion())) {
                majorVersionMatch = true;
            }
            if (myIdeaJdk.getHomePath().equals(jenvJdkModel.getHomePath())) {
                homePathMatch = true;
            }
            if (jenvJdkModel.getCanonicalPath() != null && myIdeaJdk.getHomePath().equals(jenvJdkModel.getCanonicalPath())) {
                homePathMatch = true;
            }
            if (majorVersionMatch) {
                if (homePathMatch) {
                    jenvJdkModel.setIdeaJdkInfo(myIdeaJdk.getIdeaJdkInfo());
                    if (nameMatch) {
                        jenvJdkExistsType = JenvJdkExistsType.Both;
                        break;
                    } else {
                        jenvJdkExistsType = JenvJdkExistsType.OnlyNameNotMatch;
                    }
                }
            }
        }
        return jenvJdkExistsType;
    }

    public void addToJenvJdks(Sdk jdk) {
        boolean isJenv = false;
        JenvJdkModel jenvJdkModel = null;
        for (JenvJdkModel myJenvJdk : myJenvJdks) {
            if (myJenvJdk.getName().equals(jdk.getName())) {
                if (myJenvJdk.getHomePath().equals(jdk.getHomePath())) {
                    isJenv = true;
                    jenvJdkModel = myJenvJdk;
                    break;
                }
                if (myJenvJdk.getCanonicalPath().equals(jdk.getHomePath())) {
                    isJenv = true;
                    jenvJdkModel = myJenvJdk;
                    break;
                }
            }
        }
        if (isJenv) {
            jenvJdkModel.setIdeaJdkInfo(jdk);
        } else {
            jenvJdkModel = createJenvJdkModel(jdk);
            myIdeaJdks.add(jenvJdkModel);
        }
        for (JenvJdkModel myJenvJdk : myJenvJdks) {
            JenvJdkExistsType jenvJdkExistsType = getJenvJdkExistsType(myJenvJdk);
            myJenvJdk.setExistsType(jenvJdkExistsType);
        }
        Collections.sort(myIdeaJdks);
    }

    public void changeJenvJdkName(Sdk jdk, String previousName) {
        JenvJdkModel currentIdeaJdk = null;
        for (JenvJdkModel myIdeaJdk : myIdeaJdks) {
            if (myIdeaJdk.getName().equals(previousName)) {
                currentIdeaJdk = myIdeaJdk;
                break;
            }
        }
        if (currentIdeaJdk != null) {
            currentIdeaJdk.setName(jdk.getName());
            currentIdeaJdk.setIdeaJdkInfo(jdk);
            JenvJdkExistsType jenvJdkExistsType = getIdeaJdkExistsType(jdk, currentIdeaJdk.getMajorVersion());
            currentIdeaJdk.setExistsType(jenvJdkExistsType);
        }
        for (JenvJdkModel myJenvJdk : myJenvJdks) {
            if (JenvUtils.checkIsIdea(myJenvJdk)) {
                JenvJdkExistsType jenvJdkExistsType = getJenvJdkExistsType(myJenvJdk);
                myJenvJdk.setExistsType(jenvJdkExistsType);
            }
        }
        Collections.sort(myJenvJdks);
        Collections.sort(myIdeaJdks);
    }

    public void removeFromJenvJdks(Sdk jdk) {
        String name = jdk.getName();
        List<JenvJdkModel> allIdeaAndJenvJdks = getJdksInIdeaAndInJenv();
        for (JenvJdkModel ideaAndJenvJdk : allIdeaAndJenvJdks) {
            if (name.equals(ideaAndJenvJdk.getName())) {
                myIdeaJdks.remove(ideaAndJenvJdk);
                break;
            }
        }
        for (JenvJdkModel myJenvJdk : myJenvJdks) {
            if (JenvUtils.checkIsIdea(myJenvJdk)) {
                JenvJdkExistsType jenvJdkExistsType = getJenvJdkExistsType(myJenvJdk);
                if (jenvJdkExistsType.equals(JenvJdkExistsType.Jenv)) {
                    myJenvJdk.setIdeaJdkInfo(null);
                }
                myJenvJdk.setExistsType(jenvJdkExistsType);
            }
        }
    }

    public synchronized void refreshJenvJdks() {
        myJenvJdks.clear();
        myIdeaJdks.clear();
        List<File> jenvJdkVersionFiles = JenvUtils.getJenvJdkVersionFiles();
        for (File jenvJdkVersionFile : jenvJdkVersionFiles) {
            JenvJdkModel jenvJdkModel = new JenvJdkModel();
            try {
                jenvJdkModel.setExistsType(JenvJdkExistsType.Jenv);
                jenvJdkModel.setName(jenvJdkVersionFile.getName());
                String fullVersion = jenvJdkVersionFile.getName();
                String digitVersion = JenvVersionParser.tryParser(fullVersion);
                String majorVersion = JenvVersionParser.tryParserAndGetMajorVersion(fullVersion);
                jenvJdkModel.setVersion(digitVersion);
                jenvJdkModel.setMajorVersion(majorVersion);
                jenvJdkModel.setHomePath(jenvJdkVersionFile.getPath());
                jenvJdkModel.setCanonicalPath(jenvJdkVersionFile.getCanonicalPath());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            myJenvJdks.add(jenvJdkModel);
        }
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        for (Sdk jdk : allJdks) {
            if (jdk.getSdkType() instanceof JavaSdkType) {
                JenvJdkModel jenvJdkModel = createJenvJdkModel(jdk);
                myIdeaJdks.add(jenvJdkModel);
            }
        }
        for (JenvJdkModel myJenvJdk : myJenvJdks) {
            JenvJdkExistsType jenvJdkExistsType = getJenvJdkExistsType(myJenvJdk);
            myJenvJdk.setExistsType(jenvJdkExistsType);
        }
        Collections.sort(myJenvJdks);
        Collections.sort(myIdeaJdks);
    }

    public void addAllJenvJdksToIdea(Project project) {
        List<Sdk> addJdkList = new ArrayList<>();
        List<JenvJdkModel> addAfterUpdateJdkList = new ArrayList<>();
        List<Sdk> updateJdkNameList = new ArrayList<>();
        for (JenvJdkModel jenvJdk : myJenvJdks) {
            String name = jenvJdk.getName();
            if (JenvUtils.checkIsJenv(jenvJdk)) {
                VirtualFile homePath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jenvJdk.getHomePath()));
                if (homePath != null) {
                    Sdk findJdk = ProjectJdkTable.getInstance().findJdk(name);
                    if (findJdk != null && findJdk.getHomePath() != null && !findJdk.getHomePath().equals(jenvJdk.getHomePath())) {
                        updateJdkNameList.add(findJdk);
                        addAfterUpdateJdkList.add(jenvJdk);
                    } else {
                        ApplicationManager.getApplication().invokeAndWait(() -> {
                            Sdk sdk = SdkConfigurationUtil.setupSdk(ProjectJdkTable.getInstance().getAllJdks(), homePath, JenvConstants.PROJECT_JENV_JDK_TYPE, true, null, name);
                            if (sdk != null) {
                                addJdkList.add(sdk);
                            }
                        });
                    }
                }
            } else {
                // maybe something to do
            }
        }
        // jdk name update dialog
        boolean changeName = false;
        if (!updateJdkNameList.isEmpty()) {
            JdkRenameDialog jdkRenameDialog = new JdkRenameDialog(updateJdkNameList);
            changeName = jdkRenameDialog.showAndGet();
        }
        boolean finalChangeName = changeName;
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Add Jenv JDK") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                int updateListSize = updateJdkNameList.size();
                if (finalChangeName) {
                    for (int i = 0; i < updateListSize; i++) {
                        Sdk sdk = updateJdkNameList.get(i);
                        String name = sdk.getName();
                        ProjectJdkImpl jdk = (ProjectJdkImpl) ProjectJdkTable.getInstance().findJdk(name);
                        if (jdk != null) {
                            double fraction = (double) i / updateListSize * 0.5;
                            indicator.setFraction(fraction);
                            indicator.setText(name + " is renaming");
                            ProjectJdkImpl clone = jdk.clone();
                            String changeName = jdk.getName() + JenvConstants.JDK_RENAME_SUFFIX;
                            clone.setName(changeName);
                            ApplicationManager.getApplication().invokeAndWait(() -> {
                                ProjectJdkTable.getInstance().updateJdk(jdk, clone);
                                for (JenvJdkModel jenvJdkModel : addAfterUpdateJdkList) {
                                    VirtualFile homePath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jenvJdkModel.getHomePath()));
                                    if (homePath != null) {
                                        Sdk addUpdateJenvJdk = SdkConfigurationUtil.setupSdk(ProjectJdkTable.getInstance().getAllJdks(), homePath, JenvConstants.PROJECT_JENV_JDK_TYPE, true, null, jenvJdkModel.getName());
                                        if (addUpdateJenvJdk != null) {
                                            addJdkList.add(addUpdateJenvJdk);
                                        }
                                    }
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }
                    }
                }
                int addListSize = addJdkList.size();
                for (int i = 0; i < addListSize; i++) {
                    Sdk newJdk = addJdkList.get(i);
                    indicator.setFraction((double) i / addListSize * 0.5);
                    indicator.setText("Add " + newJdk.getName());
                    ApplicationManager.getApplication().invokeAndWait(() -> SdkConfigurationUtil.addSdk(newJdk));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
