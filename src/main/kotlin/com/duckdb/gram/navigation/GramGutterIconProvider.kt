package com.duckdb.gram.navigation

import com.duckdb.gram.icons.GramIcons
import com.duckdb.gram.index.TransformerMethodIndex
import com.duckdb.gram.lexer.GramTokenTypes
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope

/**
 * Adds gutter icons on grammar rule names that have a corresponding C++ transformer method.
 * Clicking the icon navigates to the transformer function in transform_*.cpp.
 */
class GramGutterIconProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        // Only trigger on RULE_NAME leaf tokens; skip the generated inlined file
        if (element.node.elementType != GramTokenTypes.RULE_NAME) return
        if (element.containingFile?.name == "inlined_grammar.gram") return

        val ruleName = element.text.removePrefix("%")
        val project = element.project

        val targets = findTransformerElements(project, ruleName)
        if (targets.isEmpty()) return

        val marker = NavigationGutterIconBuilder.create(GramIcons.GOTO_TRANSFORMER)
            .setTargets(targets)
            .setTooltipText("Navigate to transformer: Transform$ruleName")
            .createLineMarkerInfo(element)

        result.add(marker)
    }

    companion object {
        fun findTransformerElements(project: Project, ruleName: String): List<PsiElement> {
            val scope = GlobalSearchScope.projectScope(project)
            val psiManager = PsiManager.getInstance(project)
            val methodSignature = "PEGTransformerFactory::Transform$ruleName("

            return TransformerMethodIndex.getFiles(ruleName, project, scope).mapNotNull { vFile ->
                val psiFile = psiManager.findFile(vFile) ?: return@mapNotNull null
                val idx = psiFile.text.indexOf(methodSignature)
                if (idx < 0) null else psiFile.findElementAt(idx)
            }
        }
    }
}
