package com.duckdb.gram.reference

import com.duckdb.gram.index.GramRuleNameIndex
import com.duckdb.gram.psi.GramRuleNameElement
import com.duckdb.gram.psi.GramRuleReference
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

class GramRuleReferenceImpl(element: GramRuleReference) :
    PsiReferenceBase<GramRuleReference>(element, TextRange(0, element.textLength)) {

    private val refName: String get() = element.text

    override fun resolve(): PsiElement? {
        val project = element.project
        val scope = GlobalSearchScope.allScope(project)
        val psiManager = PsiManager.getInstance(project)

        val files = GramRuleNameIndex.getFiles(refName, project, scope)
        for (vFile in files) {
            val psiFile = psiManager.findFile(vFile) ?: continue
            val nameElements = PsiTreeUtil.collectElementsOfType(psiFile, GramRuleNameElement::class.java)
            for (nameEl in nameElements) {
                if (nameEl.name == refName) return nameEl
            }
        }
        return null
    }

    override fun getVariants(): Array<Any> {
        val project = element.project
        val scope = GlobalSearchScope.allScope(project)
        val names = GramRuleNameIndex.getAllRuleNames(project, scope)
        return names.map { LookupElementBuilder.create(it) }.toTypedArray()
    }
}
