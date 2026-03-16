package com.duckdb.gram.reference

import com.duckdb.gram.psi.GramRuleReference
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

class GramRuleReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        return if (element is GramRuleReference) {
            arrayOf(GramRuleReferenceImpl(element))
        } else {
            PsiReference.EMPTY_ARRAY
        }
    }
}
