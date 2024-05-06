package com.github.oomeow.jenv.widget;

import com.github.oomeow.jenv.action.JenvFileAction;
import com.github.oomeow.jenv.action.JenvJdkModelAction;
import com.github.oomeow.jenv.icons.JenvIcons;
import com.github.oomeow.jenv.listener.StatusBarUpdateMessage;
import com.github.oomeow.jenv.model.JenvJdkModel;
import com.github.oomeow.jenv.service.JenvJdkTableService;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Disposer;
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
                JenvJdkTableService.getInstance().validateJenvJdksFiles(project);
                ListPopup popup = getPopup(projectJdkName, dataContext);
                Dimension dimension = popup.getContent().getPreferredSize();
                Point at = new Point(0, -dimension.height);
                popup.show(new RelativePoint(component, at));
                return true;
            }
        };
        clickListener.installOn(this, true);
        project.getMessageBus().connect().subscribe(StatusBarUpdateMessage.TOPIC, (StatusBarUpdateMessage) () -> {
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (projectSdk != null) {
                String name = projectSdk.getName();
                JenvJdkModel jenvJdkModel = JenvJdkTableService.getInstance().findJenvJdkByName(name);
                if (jenvJdkModel != null && jenvJdkModel.getExistsType().getIcon() != null) {
                    setIcon(jenvJdkModel.getExistsType().getIcon());
                } else {
                    setIcon(JenvIcons.JENV_JDK);
                }
                setText(name);
            } else {
                setIcon(AllIcons.General.Error);
                setText("No JDK");
            }
            statusBar.updateWidget(JENV_STATUS_BAR_ID);
        });
    }

    @Override
    public @Nullable Icon getIcon() {
        Icon icon = AllIcons.General.Error;
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null) {
            String text = projectSdk.getName();
            JenvJdkModel jenvJdkModel = JenvJdkTableService.getInstance().findJenvJdkByName(projectSdk.getName());
            if (jenvJdkModel != null && jenvJdkModel.getExistsType().getIcon() != null) {
                text = jenvJdkModel.getName();
                icon = jenvJdkModel.getExistsType().getIcon();
            } else {
                icon = JenvIcons.JENV_JDK;
            }
            setText(text);
        } else {
            setText("No JDK");
        }
        return icon;
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
        actions.add(new AnAction("Add All", "Add all jEnv JDKs to IDEA", AllIcons.Actions.AddList) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                JenvJdkTableService.getInstance().addAllJenvJdksToIdea(e.getProject());
            }
        });
        DefaultActionGroup more = DefaultActionGroup.createPopupGroup(() -> "Show All");
        List<JenvJdkModel> allJenvJdkFiles = JenvJdkTableService.getInstance().getAllJenvJdkFiles();
        for (JenvJdkModel jenvJdkFile : allJenvJdkFiles) {
            JenvFileAction jenvFileAction = new JenvFileAction(jenvJdkFile);
            more.add(jenvFileAction);
        }
        actions.add(more);
        actions.addSeparator("jEnv");
        List<JenvJdkModel> jdksInIdeaAndInJenv = JenvJdkTableService.getInstance().getJdksInIdeaAndInJenv();
        createActionWithMore(actions, jdksInIdeaAndInJenv, currentJdkName, 5);
        actions.addSeparator("Invalid jEnv");
        List<JenvJdkModel> jdksInIdeaAndInvalidJenv = JenvJdkTableService.getInstance().getJdksInIdeaAndInvalidJenv();
        createActionWithMore(actions, jdksInIdeaAndInvalidJenv, currentJdkName, 3);
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
        Disposer.dispose(this);
    }
}
