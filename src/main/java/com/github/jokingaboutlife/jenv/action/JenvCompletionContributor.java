package com.github.jokingaboutlife.jenv.action;

import com.github.jokingaboutlife.jenv.constant.JdkExistsType;
import com.github.jokingaboutlife.jenv.constant.JenvConstants;
import com.github.jokingaboutlife.jenv.model.JenvJdkModel;
import com.github.jokingaboutlife.jenv.service.JenvJdkTableService;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JenvCompletionContributor extends CompletionContributor {

    public JenvCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiFile psiFile = parameters.getOriginalFile();
                if (psiFile.getVirtualFile().getPath().endsWith(JenvConstants.VERSION_FILE)) {
                    List<JenvJdkModel> jdksInIdeaAndInJenv = JenvJdkTableService.getInstance().getJdksInIdeaAndInJenv();
                    List<JenvJdkModel> list = jdksInIdeaAndInJenv.stream().filter(o -> o.getExistsType().equals(JdkExistsType.Both)).toList();
                    PsiElement originalPosition = parameters.getOriginalPosition();
                    String text = "";
                    if (originalPosition != null) {
                        text = originalPosition.getText();
                    }
                    for (JenvJdkModel jenvJdkModel : list) {
                        result.withPrefixMatcher(text).addElement(LookupElementBuilder.create(jenvJdkModel.getName()));
                    }
                }
            }
        });
    }

}
