package com.duckdb.gram.psi

import com.duckdb.gram.parser.GramElementTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

object GramPsiFactory {
    fun createElement(node: ASTNode): PsiElement = when (node.elementType) {
        GramElementTypes.RULE            -> GramRule(node)
        GramElementTypes.RULE_NAME_ELEMENT -> GramRuleNameElement(node)
        GramElementTypes.RULE_BODY       -> GramRuleBody(node)
        GramElementTypes.RULE_REF        -> GramRuleReference(node)
        else -> throw IllegalArgumentException("Unknown element type: ${node.elementType}")
    }
}
