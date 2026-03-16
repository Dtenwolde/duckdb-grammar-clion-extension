package com.duckdb.gram.psi

import com.duckdb.gram.parser.GramElementTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.util.IncorrectOperationException

class GramRule(node: ASTNode) : ASTWrapperPsiElement(node), PsiNameIdentifierOwner {

    override fun getNameIdentifier(): PsiElement? =
        findChildByType(GramElementTypes.RULE_NAME_ELEMENT)

    override fun getName(): String? = nameIdentifier?.text?.removePrefix("%")

    override fun setName(name: String): PsiElement {
        throw IncorrectOperationException("Rename not supported")
    }
}
