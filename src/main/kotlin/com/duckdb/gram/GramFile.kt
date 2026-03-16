package com.duckdb.gram

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class GramFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, GramLanguage) {
    override fun getFileType(): FileType = GramFileType.INSTANCE
    override fun toString(): String = "DuckDB Grammar File"
}
