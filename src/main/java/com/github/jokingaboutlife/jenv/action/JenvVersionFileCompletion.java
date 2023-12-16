package com.github.jokingaboutlife.jenv.action;

import com.github.jokingaboutlife.jenv.constant.JdkExistsType;
import com.github.jokingaboutlife.jenv.constant.JenvConstants;
import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.service.JenvJdkTableService;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JenvVersionFileCompletion extends CompletionContributor {

    public JenvVersionFileCompletion() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiFile psiFile = parameters.getOriginalFile();
                if (psiFile.getVirtualFile().getPath().endsWith(JenvConstants.VERSION_FILE)) {
                    JenvJdkTableService.getInstance().validateJenvJdksFiles();
                    List<JenvJdkModel> jdksInIdeaAndInJenv = JenvJdkTableService.getInstance().getJdksInIdeaAndInJenv();
                    List<JenvJdkModel> list = jdksInIdeaAndInJenv.stream().filter(o -> o.getExistsType().equals(JdkExistsType.Both)).toList();
                    PsiElement originalPosition = parameters.getOriginalPosition();
                    String text = "";
                    if (originalPosition != null) {
                        text = originalPosition.getText().trim();
                    }
                    CompletionResultSet completionResultSet = result.withPrefixMatcher(text);
                    for (JenvJdkModel jenvJdkModel : list) {
                        String name = jenvJdkModel.getName();
                        LookupElementBuilder element = LookupElementBuilder.create(name).withInsertHandler((insertionContext, item) ->
                                WriteCommandAction.runWriteCommandAction(insertionContext.getProject(), () -> {
                                    Document document = insertionContext.getDocument();
                                    document.setText(name);
                                    FileDocumentManager.getInstance().saveDocument(document);
                                }));
                        completionResultSet.addElement(element);
                    }
                }
            }
        });
    }

}
