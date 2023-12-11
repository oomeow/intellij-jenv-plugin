package com.github.jokingaboutlife.jenv.service;

import com.github.jokingaboutlife.jenv.constant.JdkExistsType;
import com.github.jokingaboutlife.jenv.dialog.JdkRenameDialog;
import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.model.JenvRenameModel;
import com.github.jokingaboutlife.jenv.util.JenvUtils;
import com.github.jokingaboutlife.jenv.util.JenvVersionParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ThreadUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class JenvJdkTableService {

    private final List<JenvJdkModel> myJenvJdkFiles = new LinkedList<>();
    private final List<JenvJdkModel> myIdeaJdks = new LinkedList<>();

    public static JenvJdkTableService getInstance() {
        return ApplicationManager.getApplication().getService(JenvJdkTableService.class);
    }

    public @NotNull List<JenvJdkModel> getAllJenvJdkFiles() {
        return myJenvJdkFiles;
    }

    public @NotNull List<JenvJdkModel> getAllIdeaJdks() {
        return myIdeaJdks;
    }

    public @NotNull List<JenvJdkModel> getJdksInIdeaAndInJenv() {
        List<JenvJdkModel> jenvJdks = new LinkedList<>();
        for (JenvJdkModel ideaJdk : myIdeaJdks) {
            if (JenvUtils.checkIsIdeaAndIsJenv(ideaJdk)) {
                jenvJdks.add(ideaJdk);
            }
        }
        return jenvJdks;
    }

    public @NotNull List<JenvJdkModel> getJdksInIdeaAndNotInJenv() {
        List<JenvJdkModel> otherJdks = new LinkedList<>();
        for (JenvJdkModel ideaJdk : myIdeaJdks) {
            if (JenvUtils.checkIsIdeaAndNotJenv(ideaJdk)) {
                otherJdks.add(ideaJdk);
            }
        }
        return otherJdks;
    }

    public JenvJdkModel findJenvJdkByName(String jdkName) {
        JenvJdkModel jenvJdkModel = null;
        for (JenvJdkModel ideaJdk : myIdeaJdks) {
            if (ideaJdk.getName().equals(jdkName)) {
                jenvJdkModel = ideaJdk;
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
        JdkExistsType jdkExistsType = getIdeaJdkExistsType(jdk, jenvJdkModel.getMajorVersion());
        jenvJdkModel.setExistsType(jdkExistsType);
        jenvJdkModel.setHomePath(jdk.getHomePath());
        jenvJdkModel.setIdeaJdkInfo(jdk);
        return jenvJdkModel;
    }

    private JdkExistsType getIdeaJdkExistsType(Sdk jdk, String majorVersion) {
        JdkExistsType jdkExistsType = JdkExistsType.OnlyInIDEA;
        for (JenvJdkModel jenvJdkFile : myJenvJdkFiles) {
            boolean nameMatch = false;
            boolean majorVersionMatch = false;
            boolean homePathMatch = false;
            if (jenvJdkFile.getName().equals(jdk.getName())) {
                nameMatch = true;
            }
            if (jenvJdkFile.getMajorVersion().equals(majorVersion)) {
                majorVersionMatch = true;
            }
            if (jenvJdkFile.getHomePath().equals(jdk.getHomePath())) {
                homePathMatch = true;
            }
            if (jenvJdkFile.getCanonicalPath() != null && jenvJdkFile.getCanonicalPath().equals(jdk.getHomePath())) {
                homePathMatch = true;
            }
            if (majorVersionMatch) {
                if (jdkExistsType.equals(JdkExistsType.OnlyInIDEA)) {
                    jdkExistsType = JdkExistsType.OnlyMajorVersionMatch;
                }
                if (homePathMatch) {
                    if (nameMatch) {
                        jdkExistsType = JdkExistsType.Both;
                        break;
                    } else {
                        jdkExistsType = JdkExistsType.OnlyNameNotMatch;
                    }
                }
            }
        }
        return jdkExistsType;
    }

    public void addToJenvJdks(Sdk jdk) {
        JenvJdkModel jenvJdkModel = createJenvJdkModel(jdk);
        myIdeaJdks.add(jenvJdkModel);
        Collections.sort(myIdeaJdks);
    }

    public void changeJenvJdkName(Sdk jdk, String previousName) {
        JenvJdkModel currentIdeaJdk = null;
        for (JenvJdkModel ideaJdk : myIdeaJdks) {
            if (ideaJdk.getName().equals(previousName) && ideaJdk.getIdeaJdkInfo().equals(jdk)) {
                currentIdeaJdk = ideaJdk;
                break;
            }
        }
        if (currentIdeaJdk != null) {
            currentIdeaJdk.setName(jdk.getName());
            JdkExistsType jdkExistsType = getIdeaJdkExistsType(jdk, currentIdeaJdk.getMajorVersion());
            currentIdeaJdk.setExistsType(jdkExistsType);
        }
        Collections.sort(myIdeaJdks);
    }

    public void removeFromJenvJdks(Sdk jdk) {
        String name = jdk.getName();
        for (JenvJdkModel ideaJdk : myIdeaJdks) {
            if (name.equals(ideaJdk.getName()) && ideaJdk.getIdeaJdkInfo().equals(jdk)) {
                myIdeaJdks.remove(ideaJdk);
                break;
            }
        }
    }

    public synchronized void refreshJenvJdks() {
        myJenvJdkFiles.clear();
        myIdeaJdks.clear();
        List<File> jenvJdkVersionFiles = JenvUtils.getJenvJdkVersionFiles();
        for (File jenvJdkVersionFile : jenvJdkVersionFiles) {
            JenvJdkModel jenvJdkFile = new JenvJdkModel();
            try {
                jenvJdkFile.setExistsType(JdkExistsType.OnlyInJEnv);
                jenvJdkFile.setName(jenvJdkVersionFile.getName());
                String fullVersion = jenvJdkVersionFile.getName();
                String digitVersion = JenvVersionParser.tryParse(fullVersion);
                jenvJdkFile.setVersion(digitVersion);
                jenvJdkFile.setHomePath(jenvJdkVersionFile.getPath());
                jenvJdkFile.setCanonicalPath(jenvJdkVersionFile.getCanonicalPath());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            myJenvJdkFiles.add(jenvJdkFile);
        }
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        Arrays.sort(allJdks, (o1, o2) -> StringUtils.compare(o1.getName(), o2.getName()));
        for (Sdk jdk : allJdks) {
            if (jdk.getSdkType() instanceof JavaSdkType) {
                JenvJdkModel jenvJdkModel = createJenvJdkModel(jdk);
                myIdeaJdks.add(jenvJdkModel);
            }
        }
        Collections.sort(myJenvJdkFiles);
        Collections.sort(myIdeaJdks);
    }

    public void addAllJenvJdksToIdea(Project project) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Add all jEnv JDK") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                List<JenvRenameModel> renameJdkList = new ArrayList<>();
                // jenv name Map for the jenv home path
                Map<String, String> jenvHomePathNameMap = new HashMap<>();
                // jenv name Map for the jenv canonical path
                List<String> jenvCanonicalPathList = new ArrayList<>();
                myJenvJdkFiles.forEach(jenvJdkModel -> {
                    String homePath = jenvJdkModel.getHomePath();
                    jenvHomePathNameMap.put(homePath, jenvJdkModel.getName());
                    String canonicalPath = jenvJdkModel.getCanonicalPath();
                    if (canonicalPath != null && !jenvCanonicalPathList.contains(canonicalPath)) {
                        jenvCanonicalPathList.add(canonicalPath);
                    }
                });
                // delete duplicate jEnv jdk (if jEnv jdk canonical path exists, delete all.
                //      if jEnv jdk home path more than one, delete them until only exists one)
                deleteDuplicateJenvJdk(indicator, jenvCanonicalPathList);
                // analyze jEnv jdk
                analyzeJenvJdk(indicator, jenvHomePathNameMap, renameJdkList);
                // rename IDEA SDK dialog
                needToRenameDialog(indicator, project, renameJdkList);
                // add jEnv SDK to IDEA
                addJenvJdkToIDEA(indicator);
            }
        });
    }

    private void deleteDuplicateJenvJdk(@NotNull ProgressIndicator indicator, List<String> jenvCanonicalPathList) {
        indicator.setText("Find duplicate jEnv JDK");
        // remove all duplicate jenv jdks
        List<JenvJdkModel> deleteJdks = new ArrayList<>();
        Map<String, List<JenvJdkModel>> homePathGroup = myIdeaJdks.stream().filter(JenvUtils::checkIsIdeaAndIsJenv)
                .collect(Collectors.groupingBy(JenvJdkModel::getHomePath));
        homePathGroup.forEach((key, value) -> {
            if (jenvCanonicalPathList.contains(key)) {
                deleteJdks.addAll(value);
            } else if (value.size() > 1) {
                boolean existsBoth = false;
                for (JenvJdkModel jenvJdkModel : value) {
                    if (JenvUtils.checkIsBoth(jenvJdkModel)) {
                        existsBoth = true;
                    } else {
                        deleteJdks.add(jenvJdkModel);
                    }
                }
                if (!existsBoth) {
                    deleteJdks.remove(deleteJdks.size() - 1);
                }
            }
        });
        ApplicationManager.getApplication().invokeAndWait(() -> {
            indicator.setText("Remove Duplicate jEnv JDK");
            myIdeaJdks.removeIf(deleteJdks::contains);
            for (int i = 0; i < deleteJdks.size(); i++) {
                indicator.setFraction((double) (i + 1) / deleteJdks.size());
                JenvJdkModel deleteJdk = deleteJdks.get(i);
                SdkConfigurationUtil.removeSdk(deleteJdk.getIdeaJdkInfo());
            }
        });
    }

    private void analyzeJenvJdk(@NotNull ProgressIndicator indicator, Map<String, String> jenvJdkFilesMap, List<JenvRenameModel> renameJdkList) {
        indicator.setText("Analyze");
        List<JenvRenameModel> jenvJdkNeedRenameList = myIdeaJdks.stream()
                .filter(o -> o.getExistsType().equals(JdkExistsType.OnlyNameNotMatch))
                .map(jenvJdkModel -> {
                    JenvRenameModel jenvRenameModel = new JenvRenameModel();
                    jenvRenameModel.setBelongJenv(true);
                    jenvRenameModel.setIdeaSdk(jenvJdkModel.getIdeaJdkInfo());
                    String homePath = jenvJdkModel.getHomePath();
                    if (jenvJdkFilesMap.get(homePath) != null) {
                        jenvRenameModel.setChangeName(jenvJdkFilesMap.get(homePath));
                    }
                    return jenvRenameModel;
                }).toList();
        List<JenvRenameModel> notJenvJdkNeedRenameList = myIdeaJdks.stream()
                .filter(JenvUtils::checkIsIdeaAndNotJenv)
                .filter(o -> {
                    String name = o.getName();
                    Optional<JenvJdkModel> find = myJenvJdkFiles.stream().filter(k -> k.getName().equals(name)).findFirst();
                    return find.isPresent();
                })
                .map(jenvJdkModel -> {
                    JenvRenameModel jenvRenameModel = new JenvRenameModel();
                    jenvRenameModel.setBelongJenv(false);
                    jenvRenameModel.setIdeaSdk(jenvJdkModel.getIdeaJdkInfo());
                    return jenvRenameModel;
                }).toList();
        renameJdkList.addAll(jenvJdkNeedRenameList);
        renameJdkList.addAll(notJenvJdkNeedRenameList);
    }

    private void needToRenameDialog(@NotNull ProgressIndicator indicator, Project project, List<JenvRenameModel> renameJdkList) {
        if (renameJdkList.isEmpty()) {
            return;
        }
        indicator.setText("Renaming JDK");
        indicator.setFraction(1);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            JdkRenameDialog jdkRenameDialog = new JdkRenameDialog(project, renameJdkList);
            jdkRenameDialog.show();
        });
    }

    private void addJenvJdkToIDEA(@NotNull ProgressIndicator indicator) {
        for (int i = 0; i < myJenvJdkFiles.size(); i++) {
            JenvJdkModel jenvJdkFile = myJenvJdkFiles.get(i);
            indicator.setText("Find " + jenvJdkFile.getName());
            indicator.setText2(jenvJdkFile.getHomePath());
            indicator.setFraction((double) (i + 1) / myJenvJdkFiles.size());
            boolean exists = false;
            for (JenvJdkModel ideaJdk : myIdeaJdks) {
                if ((ideaJdk.getHomePath().equals(jenvJdkFile.getHomePath())
                        || (jenvJdkFile.getCanonicalPath() != null && ideaJdk.getHomePath().equals(jenvJdkFile.getCanonicalPath())))
                        || ideaJdk.getName().equals(jenvJdkFile.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                ApplicationManager.getApplication().invokeAndWait(() -> {
                    VirtualFile homeDir = VirtualFileManager.getInstance().findFileByNioPath(Path.of(jenvJdkFile.getHomePath()));
                    if (homeDir != null) {
                        indicator.setText("Add " + jenvJdkFile.getName() + " to IDEA");
                        Sdk sdk = SdkConfigurationUtil.setupSdk(ProjectJdkTable.getInstance().getAllJdks(), homeDir, JavaSdk.getInstance(), true, null, jenvJdkFile.getName());
                        if (sdk != null) {
                            SdkConfigurationUtil.addSdk(sdk);
                        }
                    }
                });
                try {
                    ThreadUtils.sleep(Duration.ofMillis(100));
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

}
