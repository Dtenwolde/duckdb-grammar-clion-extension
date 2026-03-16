package com.duckdb.gram.usage

import com.duckdb.gram.lexer.GramLexer
import com.duckdb.gram.lexer.GramTokenTypes
import com.duckdb.gram.psi.GramRuleNameElement
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet

class GramFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner = DefaultWordsScanner(
        GramLexer(),
        TokenSet.create(GramTokenTypes.RULE_NAME, GramTokenTypes.REFERENCE),
        GramTokenTypes.COMMENT_SET,
        GramTokenTypes.STRING_SET
    )

    override fun canFindUsagesFor(element: PsiElement): Boolean =
        element is GramRuleNameElement

    override fun getHelpId(element: PsiElement): String? = null

    override fun getType(element: PsiElement): String = "grammar rule"

    override fun getDescriptiveName(element: PsiElement): String =
        (element as? PsiNamedElement)?.name ?: element.text

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
        element.text
}
