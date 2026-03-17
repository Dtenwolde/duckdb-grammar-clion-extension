package com.duckdb.gram.folding

import com.duckdb.gram.psi.GramRuleBody
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class GramFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        return PsiTreeUtil.collectElementsOfType(root, GramRuleBody::class.java)
            .filter { body ->
                val range = body.textRange
                document.getLineNumber(range.startOffset) < document.getLineNumber(range.endOffset)
            }
            .map { body -> FoldingDescriptor(body.node, body.textRange) }
            .toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        val firstLine = node.text.trimStart().lines().firstOrNull()?.trim() ?: return "..."
        return if (firstLine.length > 50) firstLine.take(50) + "..." else "$firstLine ..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
