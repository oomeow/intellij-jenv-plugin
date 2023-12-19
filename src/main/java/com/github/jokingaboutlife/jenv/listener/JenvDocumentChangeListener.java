package com.github.jokingaboutlife.jenv.listener;

import com.github.jokingaboutlife.jenv.constant.JenvConstants;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class JenvDocumentChangeListener implements FileDocumentManagerListener {
    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file != null && file.getName().endsWith(JenvConstants.VERSION_FILE)) {
            String text = document.getText();
            String trimText = text.trim();
            if (!text.equals(trimText)) {
                // format content, remove whitespace to make sure jEnv command can correctly identify the JDK
                document.setText(trimText);
            }
        }
    }
}
