package com.duckdb.gram.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.util.IncorrectOperationException

class GramRuleNameElement(node: ASTNode) : ASTWrapperPsiElement(node), PsiNamedElement {
    override fun getName(): String = text.removePrefix("%")

    override fun setName(name: String): PsiElement {
        throw IncorrectOperationException("Rename not supported")
    }
}
