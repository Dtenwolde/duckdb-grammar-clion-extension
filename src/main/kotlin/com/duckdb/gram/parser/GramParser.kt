package com.duckdb.gram.parser

import com.duckdb.gram.lexer.GramTokenTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.LightPsiParser
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

/**
 * Builds PSI tree:
 *   GramFile
 *     GramRule
 *       GramRuleNameElement  (wraps RULE_NAME token)
 *       OPERATOR?            (opening paren + PARAMETER + closing paren)
 *       SEPARATOR
 *       GramRuleBody
 *         GramRuleRef*       (wraps REFERENCE tokens)
 *         ...
 */
class GramParser : PsiParser, LightPsiParser {

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        parseLight(root, builder)
        return builder.treeBuilt
    }

    override fun parseLight(root: IElementType, builder: PsiBuilder) {
        val fileMarker = builder.mark()
        while (!builder.eof()) {
            parseRule(builder)
        }
        fileMarker.done(root)
    }

    private fun parseRule(builder: PsiBuilder) {
        // skip whitespace/comments at rule start
        while (!builder.eof() && isSkippable(builder.tokenType)) {
            builder.advanceLexer()
        }
        if (builder.eof()) return
        if (builder.tokenType != GramTokenTypes.RULE_NAME) {
            builder.advanceLexer() // skip bad token
            return
        }

        val ruleMarker = builder.mark()

        // Rule name element
        val nameMarker = builder.mark()
        builder.advanceLexer()
        nameMarker.done(GramElementTypes.RULE_NAME_ELEMENT)

        // Optional parameter clause: OPERATOR('(') PARAMETER OPERATOR(')')
        while (!builder.eof() && builder.tokenType == GramTokenTypes.OPERATOR) {
            builder.advanceLexer()
        }
        while (!builder.eof() && builder.tokenType == GramTokenTypes.PARAMETER) {
            builder.advanceLexer()
        }

        // Separator '<-'
        if (!builder.eof() && builder.tokenType == GramTokenTypes.SEPARATOR) {
            builder.advanceLexer()
        }

        // Rule body: everything until next RULE_NAME token
        val bodyMarker = builder.mark()
        while (!builder.eof() && builder.tokenType != GramTokenTypes.RULE_NAME) {
            if (isSkippable(builder.tokenType)) {
                builder.advanceLexer()
                continue
            }
            if (builder.tokenType == GramTokenTypes.REFERENCE) {
                val refMarker = builder.mark()
                builder.advanceLexer()
                refMarker.done(GramElementTypes.RULE_REF)
            } else {
                builder.advanceLexer()
            }
        }
        bodyMarker.done(GramElementTypes.RULE_BODY)

        ruleMarker.done(GramElementTypes.RULE)
    }

    private fun isSkippable(type: IElementType?) =
        type == GramTokenTypes.WHITE_SPACE || type == GramTokenTypes.COMMENT
}
