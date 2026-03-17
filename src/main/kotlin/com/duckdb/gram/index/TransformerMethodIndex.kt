package com.duckdb.gram.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor

/**
 * FileBasedIndex that maps rule names → transform_*.cpp files containing the
 * corresponding PEGTransformerFactory::Transform<RuleName>( method definition.
 *
 * Replaces the previous approach of reading all .cpp files as bytes on every daemon pass.
 */
class TransformerMethodIndex : ScalarIndexExtension<String>() {

    companion object {
        @JvmField
        val NAME = ID.create<String, Void>("duckdb.gram.transformerMethod")

        private val METHOD_PATTERN = Regex("""PEGTransformerFactory::Transform([A-Z][A-Za-z0-9]*)""")

        fun getFiles(
            ruleName: String,
            project: Project,
            scope: GlobalSearchScope
        ): Collection<VirtualFile> =
            FileBasedIndex.getInstance().getContainingFiles(NAME, ruleName, scope)
    }

    override fun getName(): ID<String, Void> = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> =
        DataIndexer { fileContent ->
            val result = HashMap<String, Void?>()
            METHOD_PATTERN.findAll(fileContent.contentAsText).forEach { match ->
                result[match.groupValues[1]] = null
            }
            @Suppress("UNCHECKED_CAST")
            result as Map<String, Void>
        }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter =
        FileBasedIndex.InputFilter { file ->
            file.extension == "cpp" && file.name.startsWith("transform_")
        }

    override fun dependsOnFileContent(): Boolean = true
}
