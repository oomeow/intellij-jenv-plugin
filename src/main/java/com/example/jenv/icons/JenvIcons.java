package com.example.jenv.icons;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JenvIcons {
    public static final @NotNull Icon JENV_JDK = load("/icons/jenv.svg");
//    public static final @NotNull Icon IDEAVIM_DISABLED = load("/icons/ideavim_disabled.svg");
//    public static final @NotNull Icon TWITTER = load("/icons/twitter.svg");
//    public static final @NotNull Icon YOUTRACK = load("/icons/youtrack.svg");

    private static @NotNull Icon load(@NotNull @NonNls String path) {
        return IconManager.getInstance().getIcon(path, JenvIcons.class);
    }
}
