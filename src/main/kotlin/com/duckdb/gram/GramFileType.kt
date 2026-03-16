package com.duckdb.gram

import com.duckdb.gram.icons.GramIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class GramFileType private constructor() : LanguageFileType(GramLanguage) {
    override fun getName(): String = "DuckDB Grammar"
    override fun getDescription(): String = "DuckDB PEG grammar file"
    override fun getDefaultExtension(): String = "gram"
    override fun getIcon(): Icon = GramIcons.FILE

    companion object {
        @JvmField
        val INSTANCE = GramFileType()
    }
}
