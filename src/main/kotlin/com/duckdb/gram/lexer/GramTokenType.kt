package com.duckdb.gram.lexer

import com.duckdb.gram.GramLanguage
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class GramTokenType(debugName: String) : IElementType(debugName, GramLanguage)

object GramTokenTypes {
    @JvmField val RULE_NAME   = GramTokenType("RULE_NAME")
    @JvmField val SEPARATOR   = GramTokenType("SEPARATOR")    // <-
    @JvmField val LITERAL     = GramTokenType("LITERAL")      // '...'
    @JvmField val REFERENCE   = GramTokenType("REFERENCE")    // identifier in body
    @JvmField val OPERATOR    = GramTokenType("OPERATOR")     // ( ) !
    @JvmField val CHOICE      = GramTokenType("CHOICE")       // /
    @JvmField val QUANTIFIER  = GramTokenType("QUANTIFIER")   // ? * +
    @JvmField val COMMENT     = GramTokenType("COMMENT")      // # ...
    @JvmField val REGEX       = GramTokenType("REGEX")        // [...] or <...>
    @JvmField val PARAMETER   = GramTokenType("PARAMETER")    // param name in rule header
    @JvmField val WHITE_SPACE = GramTokenType("WHITE_SPACE")
    @JvmField val BAD_CHAR    = GramTokenType("BAD_CHAR")

    @JvmField val WHITESPACE_SET = TokenSet.create(WHITE_SPACE)
    @JvmField val COMMENT_SET    = TokenSet.create(COMMENT)
    @JvmField val STRING_SET     = TokenSet.create(LITERAL, REGEX)
}
