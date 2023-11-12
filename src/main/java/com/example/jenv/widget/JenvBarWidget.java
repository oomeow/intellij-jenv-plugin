package com.example.jenv.widget;

import com.example.jenv.action.JenvJdkModelAction;
import com.example.jenv.icons.JenvIcons;
import com.example.jenv.model.JenvSdkModel;
import com.example.jenv.service.JenvJdkTableService;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
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

    private final Project project;
    //    private boolean isLoadingJenv;
    public static final String JENV_STATUS_BAR_ID = "Jenv.Widget";
    private static final String JENV_STATUS_BAR_DISPLAY_NAME = "Jenv";

    public JenvBarWidget(Project project) {
        this.project = project;
        setIcon(JenvIcons.JENV_JDK);
        String name = ProjectRootManager.getInstance(project).getProjectSdk().getName();
        setText(name);
        setToolTipText("Jenv");
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
        String projectJdkName = ProjectRootManager.getInstance(project).getProjectSdk().getName();
        ClickListener clickListener = new ClickListener() {
            @Override
            public boolean onClick(@NotNull MouseEvent event, int clickCount) {
                Component component = event.getComponent();
                DataContext dataContext = DataManager.getInstance().getDataContext(component);
                JBPopup popup = getPopup(projectJdkName, dataContext);
                Dimension dimension = popup.getContent().getPreferredSize();
                Point at = new Point(0, -dimension.height);
                popup.show(new RelativePoint(component, at));
                return true;
            }
        };
        clickListener.installOn(this, true);
        statusBar.updateWidget(JENV_STATUS_BAR_ID);
        ProjectRootManagerImpl.getInstanceImpl(project).addProjectJdkListener(() -> {
            System.out.println("Status bar change");
            String jdkName = ProjectRootManager.getInstance(project).getProjectSdk().getName();
            setText(jdkName);
            statusBar.updateWidget(JENV_STATUS_BAR_ID);
        });
    }

    @Override
    public void dispose() {

    }

    @Override
    public @Nullable Icon getIcon() {
        return JenvIcons.JENV_JDK;
    }

    private ListPopup getPopup(String projectJdkName, DataContext dataContext) {
//        List<Action> actions = getActions()
        DefaultActionGroup actions = new DefaultActionGroup();
        List<JenvSdkModel> allJenvJdks = JenvJdkTableService.getInstance().getAllJenvJdks();
        if (!allJenvJdks.isEmpty()) {
            actions.addSeparator("Jenv");
            if (allJenvJdks.size() > 5) {
                for (int i = 0; i < allJenvJdks.size(); i++) {
                    if (i < 5) {
                        actions.add(new JenvJdkModelAction(allJenvJdks.get(i)));
                    }
                }
                DefaultActionGroup more = DefaultActionGroup.createPopupGroup(() -> IdeBundle.message("action.text.more"));
                actions.add(more);
                for (JenvSdkModel jenvJdk : allJenvJdks) {
                    more.add(new JenvJdkModelAction(jenvJdk));
                }
            }
        }
        List<JenvSdkModel> jenvJdksInIdea = JenvJdkTableService.getInstance().getJdksInIdeaAndInJenv();
        if (!jenvJdksInIdea.isEmpty()) {
            actions.addSeparator("Idea");
            for (JenvSdkModel jenvSdkModel : jenvJdksInIdea) {
                actions.add(new JenvJdkModelAction(jenvSdkModel));
            }
        }
        List<JenvSdkModel> notJenvJdksInIdea = JenvJdkTableService.getInstance().getJdksInIdeaAndNotInJenv();
        if (!notJenvJdksInIdea.isEmpty()) {
//            actions.addSeparator("Idea Not Jenv");
            for (JenvSdkModel jenvSdkModel : notJenvJdksInIdea) {
                actions.add(new JenvJdkModelAction(jenvSdkModel));
            }
        }
        ListPopup popup = JBPopupFactory.getInstance()
                .createActionGroupPopup(
                        JenvBarWidget.JENV_STATUS_BAR_DISPLAY_NAME,
                        actions,
                        dataContext,
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        false,
                        ActionPlaces.POPUP
                );
        popup.setAdText("Version 0.0.1", SwingConstants.CENTER);
        return popup;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }
}
