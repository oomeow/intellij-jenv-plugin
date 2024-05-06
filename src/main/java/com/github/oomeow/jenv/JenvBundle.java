package com.github.oomeow.jenv;

import com.github.oomeow.jenv.constant.JenvConstants;
import com.intellij.DynamicBundle;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public class JenvBundle extends DynamicBundle {

    private static final JenvBundle INSTANCE = new JenvBundle();

    private JenvBundle() {
        super(JenvConstants.BUNDLE);
    }

    public static String message(@PropertyKey(resourceBundle = JenvConstants.BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static Supplier<String> messagePointer(@PropertyKey(resourceBundle = JenvConstants.BUNDLE) String key, Object... params) {
        return INSTANCE.getLazyMessage(key, params);
    }
}
