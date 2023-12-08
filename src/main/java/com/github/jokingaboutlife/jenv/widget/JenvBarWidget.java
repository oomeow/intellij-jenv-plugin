package com.github.jokingaboutlife.jenv.widget;

import com.github.jokingaboutlife.jenv.action.JenvFileAction;
import com.github.jokingaboutlife.jenv.action.JenvJdkModelAction;
import com.github.jokingaboutlife.jenv.icons.JenvIcons;
import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.service.JenvJdkTableService;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.ui.ClickListener;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class JenvBarWidget extends TextPanel.WithIconAndArrows implements CustomStatusBarWidget {

    private final @NotNull Project project;
    public static final String JENV_STATUS_BAR_ID = "jEnv.Widget";
    public static final String JENV_STATUS_BAR_DISPLAY_NAME = "jEnv Helper";

    public JenvBarWidget(@NotNull Project project) {
        this.project = project;
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null) {
            setIcon(JenvIcons.JENV_JDK);
            setText(projectSdk.getName());
        } else {
            setIcon(AllIcons.General.Error);
            setText("No JDK");
        }
        setToolTipText("jEnv Helper");
    }

    @Override
    public @NotNull @NonNls String ID() {
        return JENV_STATUS_BAR_ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        if (project.isDisposed()) {
            return;
        }
        ClickListener clickListener = new ClickListener() {
            @Override
            public boolean onClick(@NotNull MouseEvent event, int clickCount) {
                Component component = event.getComponent();
                Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
                String projectJdkName = projectSdk != null ? projectSdk.getName() : "";
                DataContext dataContext = DataManager.getInstance().getDataContext(component);
                JBPopup popup = getPopup(projectJdkName, dataContext);
                Dimension dimension = popup.getContent().getPreferredSize();
                Point at = new Point(0, -dimension.height);
                popup.show(new RelativePoint(component, at));
                return true;
            }
        };
        clickListener.installOn(this, true);
        project.getMessageBus().connect().subscribe(ProjectJdkTable.JDK_TABLE_TOPIC, new ProjectJdkTable.Listener() {
            @Override
            public void jdkAdded(@NotNull Sdk jdk) {
                updateStatusBar(statusBar);
            }

            @Override
            public void jdkRemoved(@NotNull Sdk jdk) {
                updateStatusBar(statusBar);
            }

            @Override
            public void jdkNameChanged(@NotNull Sdk jdk, @NotNull String previousName) {
                updateStatusBar(statusBar);
            }
        });
        ProjectRootManagerImpl.getInstanceImpl(project).addProjectJdkListener(() -> updateStatusBar(statusBar));
    }

    private void updateStatusBar(@NotNull StatusBar statusBar) {
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null) {
            setIcon(JenvIcons.JENV_JDK);
            setText(projectSdk.getName());
        } else {
            setIcon(AllIcons.General.Error);
            setText("No JDK");
        }
        statusBar.updateWidget(JENV_STATUS_BAR_ID);
    }

    @Override
    public @Nullable Icon getIcon() {
        return JenvIcons.JENV_JDK;
    }

    private ListPopup getPopup(String currentJdkName, DataContext dataContext) {
        DefaultActionGroup actions = getActions(currentJdkName);
        return JBPopupFactory.getInstance()
                .createActionGroupPopup(
                        JenvBarWidget.JENV_STATUS_BAR_DISPLAY_NAME,
                        actions,
                        dataContext,
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        false,
                        ActionPlaces.POPUP
                );
    }

    @NotNull
    private static DefaultActionGroup getActions(String currentJdkName) {
        DefaultActionGroup actions = new DefaultActionGroup();
        actions.add(new DumbAwareAction("Refresh", "", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                JenvJdkTableService.getInstance().refreshJenvJdks();
            }
        });
        actions.add(new AnAction("Add All", "Add all jEnv jdks to IDEA", AllIcons.Actions.AddFile) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                JenvJdkTableService.getInstance().addAllJenvJdksToIdea(e.getProject());
            }
        });
        List<JenvJdkModel> allJenvJdkFiles = JenvJdkTableService.getInstance().getAllJenvJdkFiles();
        DefaultActionGroup more = DefaultActionGroup.createPopupGroup(() -> "Show jEnv All");
        for (JenvJdkModel jenvJdkFile : allJenvJdkFiles) {
            JenvFileAction jenvFileAction = new JenvFileAction(jenvJdkFile);
            more.add(jenvFileAction);
        }
        actions.add(more);
        actions.addSeparator();
        actions.addSeparator("jEnv");
        List<JenvJdkModel> jdksInIdeaAndInJenv = JenvJdkTableService.getInstance().getJdksInIdeaAndInJenv();
        createActionWithMore(actions, jdksInIdeaAndInJenv, currentJdkName, 5);
        actions.addSeparator("IDEA");
        List<JenvJdkModel> jdksInIdeaAndNotInJenv = JenvJdkTableService.getInstance().getJdksInIdeaAndNotInJenv();
        createActionWithMore(actions, jdksInIdeaAndNotInJenv, currentJdkName, 3);
        return actions;
    }

    private static void createActionWithMore(DefaultActionGroup actions, @NotNull List<JenvJdkModel> listData, String currentJdkName, int limit) {
        int size = listData.size();
        if (size == 0) {
            return;
        }
        boolean needMore = size > limit;
        for (int i = 0; i < size; i++) {
            if (i < limit) {
                JenvJdkModel jenvJdkModel = listData.get(i);
                boolean isCurrentJdk = jenvJdkModel.getName().equals(currentJdkName);
                JenvJdkModelAction action = new JenvJdkModelAction(jenvJdkModel, isCurrentJdk);
                actions.add(action);
            }
        }
        if (needMore) {
            DefaultActionGroup more = DefaultActionGroup.createPopupGroup(() -> IdeBundle.message("action.text.more"));
            for (int i = limit; i < size; i++) {
                JenvJdkModel jenvJdkModel = listData.get(i);
                boolean isCurrentJdk = jenvJdkModel.getName().equals(currentJdkName);
                JenvJdkModelAction action = new JenvJdkModelAction(jenvJdkModel, isCurrentJdk);
                more.add(action);
            }
            actions.add(more);
        }
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void dispose() {

    }
}
