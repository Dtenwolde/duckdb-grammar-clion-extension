package com.duckdb.gram

import com.intellij.lang.Language

object GramLanguage : Language("DuckDBGrammar") {
    private fun readResolve(): Any = GramLanguage
}
