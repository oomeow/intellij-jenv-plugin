package com.github.oomeow.jenv.icons;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JenvIcons {
    public static final @NotNull Icon JENV_JDK = load("/icons/jenv.svg");
    public static final @NotNull Icon IDEA = load("/icons/idea.svg");

    private static @NotNull Icon load(@NotNull @NonNls String path) {
        return IconLoader.getIcon(path, JenvIcons.class);
    }
}
