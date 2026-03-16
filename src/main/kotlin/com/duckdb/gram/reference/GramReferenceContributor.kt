package com.duckdb.gram.reference

import com.duckdb.gram.psi.GramRuleReference
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

class GramReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(GramRuleReference::class.java),
            GramRuleReferenceProvider()
        )
    }
}
