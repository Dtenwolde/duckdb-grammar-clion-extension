package com.duckdb.gram.structure

import com.duckdb.gram.GramFile
import com.duckdb.gram.icons.GramIcons
import com.duckdb.gram.psi.GramRule
import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.Icon

class GramStructureViewElement(private val element: NavigatablePsiElement) : StructureViewTreeElement {

    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) = element.navigate(requestFocus)
    override fun canNavigate(): Boolean = element.canNavigate()
    override fun canNavigateToSource(): Boolean = element.canNavigateToSource()

    override fun getPresentation(): ItemPresentation = object : ItemPresentation {
        override fun getPresentableText(): String? = when (element) {
            is GramFile -> element.name
            is GramRule -> element.name
            else -> element.text
        }

        override fun getIcon(unused: Boolean): Icon? = when (element) {
            is GramFile -> GramIcons.FILE
            is GramRule -> AllIcons.Nodes.Function
            else -> null
        }

        override fun getLocationString(): String? = null
    }

    override fun getChildren(): Array<TreeElement> {
        if (element !is GramFile) return emptyArray()
        return PsiTreeUtil.getChildrenOfType(element, GramRule::class.java)
            ?.map { GramStructureViewElement(it) }
            ?.toTypedArray()
            ?: emptyArray()
    }
}
