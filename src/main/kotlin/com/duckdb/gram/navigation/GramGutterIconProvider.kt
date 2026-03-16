package com.duckdb.gram.navigation

import com.duckdb.gram.icons.GramIcons
import com.duckdb.gram.lexer.GramTokenTypes
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
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
        private val REGISTER_TRANSFORM_PATTERN = Regex("""REGISTER_TRANSFORM\(Transform([A-Za-z0-9]+)\)""")
        private val METHOD_PATTERN_PREFIX = "PEGTransformerFactory::Transform"

        fun findTransformerElements(project: Project, ruleName: String): List<PsiElement> {
            val scope = GlobalSearchScope.projectScope(project)
            val psiManager = PsiManager.getInstance(project)

            // 1. Check that a transformer is registered for this rule
            val factoryFiles = FilenameIndex.getVirtualFilesByName(
                "peg_transformer_factory.cpp", scope
            )
            val registered = factoryFiles.any { vFile ->
                val text = String(vFile.contentsToByteArray())
                REGISTER_TRANSFORM_PATTERN.containsMatchIn(text) &&
                    text.contains("REGISTER_TRANSFORM(Transform$ruleName)")
            }
            if (!registered) return emptyList()

            // 2. Find the method definition in transform_*.cpp
            val results = mutableListOf<PsiElement>()
            val methodSignature = "$METHOD_PATTERN_PREFIX$ruleName("

            val allVirtualFiles = FilenameIndex.getAllFilesByExt(project, "cpp", scope)
            for (vFile in allVirtualFiles) {
                if (!vFile.name.startsWith("transform_")) continue
                val text = String(vFile.contentsToByteArray())
                val idx = text.indexOf(methodSignature)
                if (idx < 0) continue

                val psiFile = psiManager.findFile(vFile) ?: continue
                val element = psiFile.findElementAt(idx)
                if (element != null) results.add(element)
            }
            return results
        }
    }
}
