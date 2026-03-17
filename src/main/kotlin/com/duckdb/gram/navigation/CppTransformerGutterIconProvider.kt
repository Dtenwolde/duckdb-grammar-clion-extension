package com.duckdb.gram.navigation

import com.duckdb.gram.icons.GramIcons
import com.duckdb.gram.index.GramRuleNameIndex
import com.duckdb.gram.psi.GramRuleNameElement
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

/**
 * Adds gutter icons on C++ transformer method definitions (PEGTransformerFactory::TransformXxx)
 * to navigate back to the corresponding .gram rule.
 *
 * Registered for language="ObjectiveC" because CLion uses OCLanguage for all C/C++ files.
 */
class CppTransformerGutterIconProvider : RelatedItemLineMarkerProvider() {

    private val transformMethodPattern = Regex("""Transform([A-Z][A-Za-z0-9]*)""")

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val file = element.containingFile ?: return
        if (!file.name.startsWith("transform_") || !file.name.endsWith(".cpp")) return

        // Only trigger on leaf elements matching Transform<RuleName>
        if (!element.text.startsWith("Transform")) return
        if (element.children.isNotEmpty()) return  // must be a leaf
        val match = transformMethodPattern.matchEntire(element.text) ?: return

        // Confirm this is a PEGTransformerFactory definition, not an arbitrary reference.
        // In a fully-configured CMake project the parent is an OCQualifiedReferenceExpression
        // with text "PEGTransformerFactory::TransformXxx" (no parenthesis), so we just check
        // for the class qualifier rather than requiring "(" in the same node.
        val parent = element.parent ?: return
        if (!parent.text.contains("PEGTransformerFactory::")) return

        val ruleName = match.groupValues[1]

        val project = element.project
        val scope = GlobalSearchScope.allScope(project)
        val psiManager = PsiManager.getInstance(project)

        val gramFiles = GramRuleNameIndex.getFiles(ruleName, project, scope)
        val targets = mutableListOf<PsiElement>()
        for (vFile in gramFiles) {
            val psiFile = psiManager.findFile(vFile) ?: continue
            val nameElements = PsiTreeUtil.collectElementsOfType(psiFile, GramRuleNameElement::class.java)
            nameElements.firstOrNull { it.name == ruleName }?.let { targets.add(it) }
        }

        if (targets.isEmpty()) return

        val marker = NavigationGutterIconBuilder.create(GramIcons.FILE)
            .setTargets(targets)
            .setTooltipText("Navigate to grammar rule: $ruleName")
            .createLineMarkerInfo(element)

        result.add(marker)
    }
}
