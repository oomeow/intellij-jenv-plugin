package com.github.jokingaboutlife.jenv.service;

import com.github.jokingaboutlife.jenv.constant.JdkExistsType;
import com.github.jokingaboutlife.jenv.constant.JenvConstants;
import com.github.jokingaboutlife.jenv.dialog.JdkRenameDialog;
import com.github.jokingaboutlife.jenv.listener.StatusBarUpdateMessage;
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
import com.intellij.openapi.projectRoots.impl.UnknownSdkEditorNotification;
import com.intellij.openapi.projectRoots.impl.UnknownSdkFix;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
        return myIdeaJdks.stream().filter(JenvUtils::checkIsIdeaAndIsJenv).collect(Collectors.toList());
    }

    public @NotNull List<JenvJdkModel> getJdksInIdeaAndNotInJenv() {
        return myIdeaJdks.stream().filter(JenvUtils::checkIsIdeaAndNotJenv).collect(Collectors.toList());
    }


    public List<JenvJdkModel> getJdksInIdeaAndInvalidJenv() {
        return myIdeaJdks.stream().filter(o -> o.getExistsType().equals(JdkExistsType.JEnvHomePathInvalid)).collect(Collectors.toList());
    }

    public JenvJdkModel findJenvJdkByName(String jdkName) {
        return myIdeaJdks.stream().filter(o -> o.getName().equals(jdkName)).findFirst().orElse(null);
    }

    public void addToJenvJdks(Sdk jdk) {
        JenvJdkModel jenvJdkModel = createJenvJdkModel(jdk);
        myIdeaJdks.add(jenvJdkModel);
        Collections.sort(myIdeaJdks);
    }

    @NotNull
    private JenvJdkModel createJenvJdkModel(Sdk jdk) {
        JenvJdkModel jenvJdkModel = new JenvJdkModel();
        jenvJdkModel.setName(jdk.getName());
        jenvJdkModel.setHomePath(jdk.getHomePath());
        jenvJdkModel.setVersion(JenvVersionParser.tryParse(jdk.getVersionString()));
        jenvJdkModel.setExistsType(getIdeaJdkExistsType(jdk, jenvJdkModel.getMajorVersion()));
        jenvJdkModel.setIdeaJdkInfo(jdk);
        return jenvJdkModel;
    }

    private JdkExistsType getIdeaJdkExistsType(Sdk jdk, String majorVersion) {
        JdkExistsType jdkExistsType = JdkExistsType.OnlyInIDEA;
        for (JenvJdkModel jenvJdkFile : myJenvJdkFiles) {
            boolean nameMatch = false;
            boolean majorVersionMatch = false;
            boolean homePathMatch = false;
            String homePath = jdk.getHomePath();
            if (homePath != null && homePath.contains(JenvConstants.JENV_VERSIONS_DIR) && !FileUtil.exists(homePath)) {
                jdkExistsType = JdkExistsType.JEnvHomePathInvalid;
                break;
            }
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

    public void changeJenvJdkName(Sdk jdk, String previousName) {
        for (JenvJdkModel ideaJdk : myIdeaJdks) {
            if (ideaJdk.getName().equals(previousName) && ideaJdk.getIdeaJdkInfo().equals(jdk)) {
                ideaJdk.setName(jdk.getName());
                JdkExistsType jdkExistsType = getIdeaJdkExistsType(jdk, ideaJdk.getMajorVersion());
                ideaJdk.setExistsType(jdkExistsType);
                break;
            }
        }
        Collections.sort(myIdeaJdks);
    }

    public void removeFromJenvJdks(Sdk jdk) {
        myIdeaJdks.removeIf(o -> o.getName().equals(jdk.getName()) && o.getIdeaJdkInfo().equals(jdk));
    }

    public void validateJenvJdksFiles(Project project) {
        List<String> jenvJdkVersionFiles = JenvUtils.getJenvJdkVersionFiles().stream().map(File::getPath).toList();
        boolean jenvFilesChanged = false;
        if (jenvJdkVersionFiles.size() != myJenvJdkFiles.size()) {
            jenvFilesChanged = true;
        } else {
            List<String> collect = myJenvJdkFiles.stream().map(JenvJdkModel::getHomePath).toList();
            if (!new HashSet<>(jenvJdkVersionFiles).containsAll(collect)) {
                jenvFilesChanged = true;
            }
        }
        if (jenvFilesChanged) {
            refreshJenvJdkFiles();
            String projectJdkName;
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (projectSdk != null) {
                projectJdkName = projectSdk.getName();
            } else {
                projectJdkName = null;
            }
            myIdeaJdks.stream().filter(o -> o.getExistsType().equals(JdkExistsType.JEnvHomePathInvalid) || JenvUtils.checkIsIdeaAndIsJenv(o))
                    .forEach(o -> {
                        if (o.getExistsType().equals(JdkExistsType.JEnvHomePathInvalid) && FileUtil.exists(o.getHomePath())) {
                            o.setExistsType(getIdeaJdkExistsType(o.getIdeaJdkInfo(), o.getMajorVersion()));
                            if (projectJdkName != null && projectJdkName.equals(o.getName())) {
                                // current project JDK
                                ApplicationManager.getApplication().invokeLater(() -> ApplicationManager.getApplication().runWriteAction(() -> {
                                    Sdk jdk = ProjectRootManager.getInstance(project).getProjectSdk();
                                    if (jdk != null) {
                                        JavaSdk.getInstance().setupSdkPaths(jdk);
                                    }
                                    List<UnknownSdkFix> notifications = UnknownSdkEditorNotification.getInstance(project).getNotifications();
                                    if (!notifications.isEmpty()) {
                                        // reset project JDK (remove idea original invalid JDK banner)
                                        // I can't find another way to remove idea original invalid JDK banner.
                                        // temporarily use the following code to achieve. (set project null and reset the original JDK again)
                                        SdkConfigurationUtil.setDirectoryProjectSdk(project, null);
                                        AppExecutorUtil.getAppScheduledExecutorService().schedule(() -> {
                                            ApplicationManager.getApplication().invokeLater(() -> {
                                                ApplicationManager.getApplication().runWriteAction(() -> {
                                                    SdkConfigurationUtil.setDirectoryProjectSdk(project, jdk);
                                                });
                                            });
                                        }, 3, TimeUnit.SECONDS);
                                    }
                                }));
                            }
                        } else if (!FileUtil.exists(o.getHomePath())) {
                            o.setExistsType(JdkExistsType.JEnvHomePathInvalid);
                        }
                    });
            EditorNotifications.getInstance(project).updateAllNotifications();
            ApplicationManager.getApplication().getMessageBus().syncPublisher(StatusBarUpdateMessage.TOPIC).updateStatusBar();
        }
    }

    public synchronized void refreshJenvJdks() {
        refreshJenvJdkFiles();
        myIdeaJdks.clear();
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        Arrays.sort(allJdks, (o1, o2) -> StringUtils.compare(o1.getName(), o2.getName()));
        for (Sdk jdk : allJdks) {
            if (jdk.getSdkType() instanceof JavaSdkType) {
                JenvJdkModel jenvJdkModel = createJenvJdkModel(jdk);
                myIdeaJdks.add(jenvJdkModel);
            }
        }
        Collections.sort(myIdeaJdks);
    }

    private void refreshJenvJdkFiles() {
        myJenvJdkFiles.clear();
        List<File> jenvJdkVersionFiles = JenvUtils.getJenvJdkVersionFiles();
        for (File jenvJdkVersionFile : jenvJdkVersionFiles) {
            JenvJdkModel jenvJdkFile = new JenvJdkModel();
            try {
                jenvJdkFile.setName(jenvJdkVersionFile.getName());
                jenvJdkFile.setHomePath(jenvJdkVersionFile.getPath());
                jenvJdkFile.setCanonicalPath(jenvJdkVersionFile.getCanonicalPath());
                jenvJdkFile.setVersion(JenvVersionParser.tryParse(jenvJdkVersionFile.getName()));
                jenvJdkFile.setExistsType(JdkExistsType.OnlyInJEnv);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            myJenvJdkFiles.add(jenvJdkFile);
        }
        Collections.sort(myJenvJdkFiles);
    }

    public void addAllJenvJdksToIdea(Project project) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Add all jEnv JDK") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                List<JenvRenameModel> renameJdkList = new ArrayList<>();
                Map<String, String> jenvHomePathNameMap = new HashMap<>();
                List<String> jenvCanonicalPathList = new ArrayList<>();
                myJenvJdkFiles.forEach(jenvJdkModel -> {
                    String homePath = jenvJdkModel.getHomePath();
                    jenvHomePathNameMap.put(homePath, jenvJdkModel.getName());
                    String canonicalPath = jenvJdkModel.getCanonicalPath();
                    if (canonicalPath != null && !jenvCanonicalPathList.contains(canonicalPath)) {
                        jenvCanonicalPathList.add(canonicalPath);
                    }
                });
                removeDuplicateJenvJdk(indicator, jenvCanonicalPathList);
                analyzeJenvJdk(indicator, jenvHomePathNameMap, renameJdkList);
                needToRenameDialog(indicator, project, renameJdkList);
                addJenvJdkToIDEA(indicator);
            }
        });
    }

    /**
     * delete jEnv JDK
     * 1. home path has removed, delete all.
     * 2. canonical path exists, delete all.
     * 3. multiple same home path, delete them until only one exists
     */
    private void removeDuplicateJenvJdk(@NotNull ProgressIndicator indicator, List<String> jenvCanonicalPathList) {
        indicator.setText("Find duplicate jEnv JDK");
        List<JenvJdkModel> removeJdks = new ArrayList<>();
        Map<String, List<JenvJdkModel>> homePathGroup = myIdeaJdks.stream().filter(JenvUtils::checkIsIdeaAndIsJenv)
                .collect(Collectors.groupingBy(JenvJdkModel::getHomePath));
        for (Map.Entry<String, List<JenvJdkModel>> entry : homePathGroup.entrySet()) {
            String homePath = entry.getKey();
            List<JenvJdkModel> jenvJdkModels = entry.getValue();
            if (jenvCanonicalPathList.contains(homePath)) {
                removeJdks.addAll(jenvJdkModels);
            } else if (jenvJdkModels.size() > 1) {
                boolean existsBoth = false;
                for (JenvJdkModel jenvJdkModel : jenvJdkModels) {
                    if (JenvUtils.checkIsBoth(jenvJdkModel)) {
                        existsBoth = true;
                    } else {
                        removeJdks.add(jenvJdkModel);
                    }
                }
                if (!existsBoth) {
                    removeJdks.remove(removeJdks.size() - 1);
                }
            }
        }
        ApplicationManager.getApplication().invokeAndWait(() -> {
            indicator.setText("Remove duplicate jEnv JDK");
            myIdeaJdks.removeIf(removeJdks::contains);
            for (int i = 0; i < removeJdks.size(); i++) {
                indicator.setFraction((double) (i + 1) / removeJdks.size());
                JenvJdkModel deleteJdk = removeJdks.get(i);
                SdkConfigurationUtil.removeSdk(deleteJdk.getIdeaJdkInfo());
            }
        });
    }

    private void analyzeJenvJdk(@NotNull ProgressIndicator indicator, Map<String, String> jenvHomePathMap, List<JenvRenameModel> renameJdkList) {
        indicator.setText("Analyze");
        List<JenvRenameModel> jenvJdkNeedRenameList = myIdeaJdks.stream()
                .filter(o -> o.getExistsType().equals(JdkExistsType.OnlyNameNotMatch))
                .map(jenvJdkModel -> {
                    JenvRenameModel jenvRenameModel = new JenvRenameModel();
                    jenvRenameModel.setBelongJenv(true);
                    jenvRenameModel.setIdeaSdk(jenvJdkModel.getIdeaJdkInfo());
                    String homePath = jenvJdkModel.getHomePath();
                    if (jenvHomePathMap.get(homePath) != null) {
                        jenvRenameModel.setChangeName(jenvHomePathMap.get(homePath));
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
                if (ideaJdk.getHomePath().equals(jenvJdkFile.getHomePath()) || ideaJdk.getName().equals(jenvJdkFile.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                ApplicationManager.getApplication().invokeAndWait(() -> {
                    VirtualFile homeDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(jenvJdkFile.getHomePath());
                    if (homeDir != null) {
                        indicator.setText("Add " + jenvJdkFile.getName() + " to IDEA");
                        Sdk sdk = SdkConfigurationUtil.setupSdk(ProjectJdkTable.getInstance().getAllJdks(), homeDir, JavaSdk.getInstance(), true, null, jenvJdkFile.getName());
                        if (sdk != null) {
                            SdkConfigurationUtil.addSdk(sdk);
                        }
                    }
                });
                TimeoutUtil.sleep(50);
            }
        }
    }

}
