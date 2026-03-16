package com.duckdb.gram.highlighting

import com.duckdb.gram.lexer.GramLexer
import com.duckdb.gram.lexer.GramTokenTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class GramSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        @JvmField
        val RULE_NAME = createTextAttributesKey(
            "GRAM_RULE_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
        )

        @JvmField
        val SEPARATOR = createTextAttributesKey(
            "GRAM_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

        @JvmField
        val LITERAL = createTextAttributesKey(
            "GRAM_LITERAL", DefaultLanguageHighlighterColors.STRING
        )

        @JvmField
        val REFERENCE = createTextAttributesKey(
            "GRAM_REFERENCE", DefaultLanguageHighlighterColors.IDENTIFIER
        )

        @JvmField
        val OPERATOR = createTextAttributesKey(
            "GRAM_OPERATOR", DefaultLanguageHighlighterColors.PARENTHESES
        )

        @JvmField
        val CHOICE = createTextAttributesKey(
            "GRAM_CHOICE", DefaultLanguageHighlighterColors.KEYWORD
        )

        @JvmField
        val QUANTIFIER = createTextAttributesKey(
            "GRAM_QUANTIFIER", DefaultLanguageHighlighterColors.KEYWORD
        )

        @JvmField
        val COMMENT = createTextAttributesKey(
            "GRAM_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT
        )

        @JvmField
        val REGEX = createTextAttributesKey(
            "GRAM_REGEX", DefaultLanguageHighlighterColors.STRING
        )

        @JvmField
        val PARAMETER = createTextAttributesKey(
            "GRAM_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER
        )

        @JvmField
        val BAD_CHAR = createTextAttributesKey(
            "GRAM_BAD_CHAR", HighlighterColors.BAD_CHARACTER
        )

        private val RULE_NAME_KEYS  = arrayOf(RULE_NAME)
        private val SEPARATOR_KEYS  = arrayOf(SEPARATOR)
        private val LITERAL_KEYS    = arrayOf(LITERAL)
        private val REFERENCE_KEYS  = arrayOf(REFERENCE)
        private val OPERATOR_KEYS   = arrayOf(OPERATOR)
        private val CHOICE_KEYS     = arrayOf(CHOICE)
        private val QUANTIFIER_KEYS = arrayOf(QUANTIFIER)
        private val COMMENT_KEYS    = arrayOf(COMMENT)
        private val REGEX_KEYS      = arrayOf(REGEX)
        private val PARAMETER_KEYS  = arrayOf(PARAMETER)
        private val BAD_CHAR_KEYS   = arrayOf(BAD_CHAR)
        private val EMPTY           = emptyArray<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer = GramLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        GramTokenTypes.RULE_NAME  -> RULE_NAME_KEYS
        GramTokenTypes.SEPARATOR  -> SEPARATOR_KEYS
        GramTokenTypes.LITERAL    -> LITERAL_KEYS
        GramTokenTypes.REFERENCE  -> REFERENCE_KEYS
        GramTokenTypes.OPERATOR   -> OPERATOR_KEYS
        GramTokenTypes.CHOICE     -> CHOICE_KEYS
        GramTokenTypes.QUANTIFIER -> QUANTIFIER_KEYS
        GramTokenTypes.COMMENT    -> COMMENT_KEYS
        GramTokenTypes.REGEX      -> REGEX_KEYS
        GramTokenTypes.PARAMETER  -> PARAMETER_KEYS
        GramTokenTypes.BAD_CHAR   -> BAD_CHAR_KEYS
        else -> EMPTY
    }
}
