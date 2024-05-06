package com.github.oomeow.jenv.completion;

import com.github.oomeow.jenv.constant.JdkExistsType;
import com.github.oomeow.jenv.constant.JenvConstants;
import com.github.oomeow.jenv.icons.JenvIcons;
import com.github.oomeow.jenv.model.JenvJdkModel;
import com.github.oomeow.jenv.service.JenvJdkTableService;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
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
                    Project project = psiFile.getProject();
                    JenvJdkTableService.getInstance().validateJenvJdksFiles(project);
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
                        LookupElementBuilder element = LookupElementBuilder.create(name)
                                .withIcon(JenvIcons.JENV_JDK)
                                .withInsertHandler((insertionContext, item) ->
                                        WriteCommandAction.runWriteCommandAction(insertionContext.getProject(), () -> {
                                            Document document = insertionContext.getDocument();
                                            document.setText(name);
                                            FileDocumentManager.getInstance().saveDocument(document);
                                        })
                                );
                        completionResultSet.addElement(element);
                    }
                }
            }
        });
    }

}
