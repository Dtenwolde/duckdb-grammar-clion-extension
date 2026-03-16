package com.duckdb.gram.psi

import com.duckdb.gram.reference.GramRuleReferenceImpl
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

class GramRuleReference(node: ASTNode) : ASTWrapperPsiElement(node) {
    override fun getReference(): PsiReference = GramRuleReferenceImpl(this)
}
