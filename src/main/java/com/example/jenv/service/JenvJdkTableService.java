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

    public JenvJdkModel findJenvJdkByName(String jdkName) {
        JenvJdkModel jenvJdkModel = null;
        for (JenvJdkModel myIdeaJdk : myIdeaJdks) {
            if (myIdeaJdk.getName().equals(jdkName)) {
                jenvJdkModel = myIdeaJdk;
                break;
            }
        }
        return jenvJdkModel;
    }

    @NotNull
    private JenvJdkModel createJenvJdkModel(Sdk jdk) {
        JenvJdkModel jenvJdkModel = new JenvJdkModel();
        jenvJdkModel.setName(jdk.getName());
        String versionString = jdk.getVersionString();
        jenvJdkModel.setVersion(JenvVersionParser.tryParse(versionString));
        JenvJdkExistsType jenvJdkExistsType = getIdeaJdkExistsType(jdk, jenvJdkModel.getMajorVersion());
        jenvJdkModel.setExistsType(jenvJdkExistsType);
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
                if (jenvJdkExistsType.equals(JenvJdkExistsType.Idea)) {
                    jenvJdkExistsType = JenvJdkExistsType.OnlyMajorVersionMatch;
                }
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
        JenvJdkModel jenvJdkModel = createJenvJdkModel(jdk);
        myIdeaJdks.add(jenvJdkModel);
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
            JenvJdkExistsType jenvJdkExistsType = getIdeaJdkExistsType(jdk, currentIdeaJdk.getMajorVersion());
            currentIdeaJdk.setExistsType(jenvJdkExistsType);
        }
        Collections.sort(myIdeaJdks);
    }

    public void removeFromJenvJdks(Sdk jdk) {
        String name = jdk.getName();
        for (JenvJdkModel jenvJdkModel : myIdeaJdks) {
            if (name.equals(jenvJdkModel.getName())) {
                myIdeaJdks.remove(jenvJdkModel);
                break;
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
                String digitVersion = JenvVersionParser.tryParse(fullVersion);
                jenvJdkModel.setVersion(digitVersion);
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
        Collections.sort(myJenvJdks);
        Collections.sort(myIdeaJdks);
    }

    public void addAllJenvJdksToIdea(Project project) {
        List<Sdk> addJdkList = new ArrayList<>();
        List<Sdk> updateJdkNameDialogList = new ArrayList<>();
        List<JenvJdkModel> addNewJenvJdkList = new ArrayList<>();
        List<JenvJdkModel> addAfterUpdateJdkList = new ArrayList<>();
        for (JenvJdkModel jenvJdk : myJenvJdks) {
            String name = jenvJdk.getName();
            boolean exists = false;
            boolean onlyNeedUpdate = false;
            for (JenvJdkModel myIdeaJdk : myIdeaJdks) {
                if (myIdeaJdk.getHomePath().equals(jenvJdk.getHomePath()) || myIdeaJdk.getHomePath().equals(jenvJdk.getCanonicalPath())) {
                    if (myIdeaJdk.getName().equals(name)) {
                        exists = true;
                        onlyNeedUpdate = false;
                        break;
                    }
                    // todo: same home path, different name, only need to change jdk name
                }
                if (myIdeaJdk.getName().equals(name)) {
                    onlyNeedUpdate = true;
                    addAfterUpdateJdkList.add(jenvJdk);
                    updateJdkNameDialogList.add(myIdeaJdk.getIdeaJdkInfo());
                }
            }
            if (!exists && !onlyNeedUpdate) {
                addNewJenvJdkList.add(jenvJdk);
            }
        }
        // jdk name update dialog
        boolean changeName = false;
        if (!updateJdkNameDialogList.isEmpty()) {
            JdkRenameDialog jdkRenameDialog = new JdkRenameDialog(updateJdkNameDialogList);
            changeName = jdkRenameDialog.showAndGet();
        }
        boolean finalChangeName = changeName;
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Add all Jenv JDK") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                int updateListSize = updateJdkNameDialogList.size();
                if (finalChangeName) {
                    for (int i = 0; i < updateListSize; i++) {
                        Sdk sdk = updateJdkNameDialogList.get(i);
                        String name = sdk.getName();
                        ProjectJdkImpl jdk = (ProjectJdkImpl) ProjectJdkTable.getInstance().findJdk(name);
                        if (jdk != null) {
                            indicator.setFraction((double) i / updateListSize);
                            indicator.setText("Rename JDK name" + name + " to " + name + JenvConstants.JDK_RENAME_SUFFIX);
                            ProjectJdkImpl clone = jdk.clone();
                            String changeName = jdk.getName() + JenvConstants.JDK_RENAME_SUFFIX;
                            clone.setName(changeName);
                            ApplicationManager.getApplication().invokeAndWait(() -> {
                                ProjectJdkTable.getInstance().updateJdk(jdk, clone);
                                for (JenvJdkModel jenvJdkModel : addAfterUpdateJdkList) {
                                    VirtualFile homePath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jenvJdkModel.getHomePath()));
                                    if (homePath != null) {
                                        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
                                        Sdk addUpdateJenvJdk = SdkConfigurationUtil.setupSdk(allJdks, homePath, JenvConstants.PROJECT_JENV_JDK_TYPE, true, null, jenvJdkModel.getName());
                                        if (addUpdateJenvJdk != null) {
                                            addJdkList.add(addUpdateJenvJdk);
                                        }
                                    }
                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }
                    }
                }
                for (JenvJdkModel jenvJdkModel : addNewJenvJdkList) {
                    VirtualFile homePath = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jenvJdkModel.getHomePath()));
                    if (homePath != null) {
                        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
                        Sdk addJenvJdk = SdkConfigurationUtil.setupSdk(allJdks, homePath, JenvConstants.PROJECT_JENV_JDK_TYPE, true, null, jenvJdkModel.getName());
                        if (addJenvJdk != null) {
                            addJdkList.add(addJenvJdk);
                        }
                    }
                }
                int addListSize = addJdkList.size();
                for (int j = 0; j < addListSize; j++) {
                    Sdk newJdk = addJdkList.get(j);
                    indicator.setFraction((double) j / addListSize);
                    indicator.setText("Add " + newJdk.getName());
                    ApplicationManager.getApplication().invokeAndWait(() -> SdkConfigurationUtil.addSdk(newJdk));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
