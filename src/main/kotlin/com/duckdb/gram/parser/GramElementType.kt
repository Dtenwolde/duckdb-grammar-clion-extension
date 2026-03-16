package com.duckdb.gram.parser

import com.duckdb.gram.GramLanguage
import com.intellij.psi.tree.IElementType

class GramElementType(debugName: String) : IElementType(debugName, GramLanguage)

object GramElementTypes {
    @JvmField val RULE       = GramElementType("RULE")
    @JvmField val RULE_NAME_ELEMENT = GramElementType("RULE_NAME_ELEMENT")
    @JvmField val RULE_BODY  = GramElementType("RULE_BODY")
    @JvmField val RULE_REF   = GramElementType("RULE_REF")
}
