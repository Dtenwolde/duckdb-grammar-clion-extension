package com.duckdb.gram.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor

/**
 * FileBasedIndex that maps rule names → virtual files containing that rule.
 * Used for cross-file Go-to-Definition and Find Usages.
 */
class GramRuleNameIndex : ScalarIndexExtension<String>() {

    companion object {
        @JvmField
        val NAME = ID.create<String, Void>("duckdb.gram.ruleName")

        private val RULE_PATTERN = Regex(
            """^(%?[A-Za-z][A-Za-z0-9_]*)(?:\([A-Za-z0-9_]+\))?\s*<-""",
            RegexOption.MULTILINE
        )

        fun getFiles(
            ruleName: String,
            project: Project,
            scope: GlobalSearchScope
        ): Collection<VirtualFile> =
            FileBasedIndex.getInstance()
                .getContainingFiles(NAME, ruleName, scope)

        fun getAllRuleNames(project: Project, scope: GlobalSearchScope): Collection<String> =
            FileBasedIndex.getInstance().getAllKeys(NAME, project)
    }

    override fun getName(): ID<String, Void> = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> =
        DataIndexer { fileContent ->
            val result = HashMap<String, Void?>()
            RULE_PATTERN.findAll(fileContent.contentAsText).forEach { match ->
                val rawName = match.groupValues[1]
                result[rawName.removePrefix("%")] = null
            }
            @Suppress("UNCHECKED_CAST")
            result as Map<String, Void>
        }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getVersion(): Int = 3

    override fun getInputFilter(): FileBasedIndex.InputFilter =
        FileBasedIndex.InputFilter { file ->
            file.extension == "gram" && file.name != "inlined_grammar.gram"
        }

    override fun dependsOnFileContent(): Boolean = true
}
