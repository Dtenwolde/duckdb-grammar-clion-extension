package com.duckdb.gram.lexer

import com.intellij.testFramework.LexerTestCase

/**
 * Tests for GramLexer's three-state machine (RULE_NAME → RULE_SEPARATOR → RULE_DEFINITION).
 *
 * Each test checks the full token stream for a given input snippet.
 * Token format: TOKEN_TYPE ('text')
 */
class GramLexerTest : LexerTestCase() {

    override fun createLexer() = GramLexer()
    override fun getDirPath() = ""

    private fun tokens(text: String) = printTokens(text, 0, createLexer())

    // ── Basic rule structure ──────────────────────────────────────────────────

    fun testSimpleRule() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            LITERAL (''hello'')

            """.trimIndent(),
            tokens("rule <- 'hello'")
        )
    }

    fun testRuleWithReference() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REFERENCE ('other')

            """.trimIndent(),
            tokens("rule <- other")
        )
    }

    fun testMultipleRules() {
        assertEquals(
            """
            RULE_NAME ('a')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            LITERAL (''x'')
            WHITE_SPACE ('
            ')
            RULE_NAME ('b')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            LITERAL (''y'')

            """.trimIndent(),
            tokens("a <- 'x'\nb <- 'y'")
        )
    }

    // ── Parameterised rule header ─────────────────────────────────────────────

    fun testRuleWithParameter() {
        assertEquals(
            """
            RULE_NAME ('rule')
            OPERATOR ('(')
            PARAMETER ('param')
            OPERATOR (')')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            LITERAL (''x'')

            """.trimIndent(),
            tokens("rule(param) <- 'x'")
        )
    }

    // ── Quantifiers ───────────────────────────────────────────────────────────

    fun testQuantifierStar() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REFERENCE ('item')
            QUANTIFIER ('*')

            """.trimIndent(),
            tokens("rule <- item*")
        )
    }

    fun testQuantifierPlus() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REFERENCE ('item')
            QUANTIFIER ('+')

            """.trimIndent(),
            tokens("rule <- item+")
        )
    }

    fun testQuantifierOptional() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REFERENCE ('item')
            QUANTIFIER ('?')

            """.trimIndent(),
            tokens("rule <- item?")
        )
    }

    // ── Choice (/) and multi-line continuation ────────────────────────────────

    fun testChoiceOnOneLine() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REFERENCE ('a')
            WHITE_SPACE (' ')
            CHOICE ('/')
            WHITE_SPACE (' ')
            REFERENCE ('b')

            """.trimIndent(),
            tokens("rule <- a / b")
        )
    }

    fun testMultiLineRuleTrailingChoice() {
        // A trailing '/' sets inOrClause=true so the following newline does NOT
        // reset state to RULE_NAME — the rule continues on the next line.
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REFERENCE ('a')
            WHITE_SPACE (' ')
            CHOICE ('/')
            WHITE_SPACE ('
                ')
            REFERENCE ('b')

            """.trimIndent(),
            tokens("rule <- a /\n    b")
        )
    }

    fun testMultiLineRuleLeadingChoice() {
        // A leading '/' on a continuation line must be highlighted as CHOICE, not BAD_CHAR.
        // This is the common DuckDB grammar style where each alternative starts with '/'.
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REFERENCE ('a')
            WHITE_SPACE ('
                ')
            CHOICE ('/')
            WHITE_SPACE (' ')
            REFERENCE ('b')
            WHITE_SPACE ('
                ')
            CHOICE ('/')
            WHITE_SPACE (' ')
            REFERENCE ('c')

            """.trimIndent(),
            tokens("rule <- a\n    / b\n    / c")
        )
    }

    // ── Grouping and negation ─────────────────────────────────────────────────

    fun testGrouping() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            OPERATOR ('(')
            REFERENCE ('a')
            WHITE_SPACE (' ')
            REFERENCE ('b')
            OPERATOR (')')
            QUANTIFIER ('+')

            """.trimIndent(),
            tokens("rule <- (a b)+")
        )
    }

    fun testNegation() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            OPERATOR ('!')
            REFERENCE ('item')

            """.trimIndent(),
            tokens("rule <- !item")
        )
    }

    // ── Regex tokens ──────────────────────────────────────────────────────────

    fun testCharacterClass() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REGEX ('[a-z]')

            """.trimIndent(),
            tokens("rule <- [a-z]")
        )
    }

    fun testAngleBracketRegex() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REGEX ('<digits>')

            """.trimIndent(),
            tokens("rule <- <digits>")
        )
    }

    // ── Comments ─────────────────────────────────────────────────────────────

    fun testStandaloneComment() {
        assertEquals(
            """
            COMMENT ('# this is a comment')
            WHITE_SPACE ('
            ')
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            LITERAL (''x'')

            """.trimIndent(),
            tokens("# this is a comment\nrule <- 'x'")
        )
    }

    fun testInlineComment() {
        // Comment in rule body terminates at end of line; state stays RULE_DEFINITION.
        // The newline after the comment then terminates the rule (bracketCount=0, !inOrClause).
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REFERENCE ('item')
            WHITE_SPACE (' ')
            COMMENT ('# inline')

            """.trimIndent(),
            tokens("rule <- item # inline")
        )
    }

    // ── Percent-prefixed rules ────────────────────────────────────────────────

    fun testPercentRule() {
        assertEquals(
            """
            RULE_NAME ('%root')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            REFERENCE ('stmt')

            """.trimIndent(),
            tokens("%root <- stmt")
        )
    }

    // ── Escaped literal ───────────────────────────────────────────────────────

    fun testLiteralWithEscape() {
        assertEquals(
            """
            RULE_NAME ('rule')
            WHITE_SPACE (' ')
            SEPARATOR ('<-')
            WHITE_SPACE (' ')
            LITERAL (''it\'s'')

            """.trimIndent(),
            tokens("rule <- 'it\\'s'")
        )
    }

    // ── Lexer restart correctness ─────────────────────────────────────────────

    fun testLexerRestartIsCorrect() {
        // LexerTestCase.checkCorrectRestart verifies that incremental re-lex
        // produces the same token stream as a full lex from the start.
        checkCorrectRestart("rule <- (a / b)+ 'x' # comment\nother <- item?")
    }
}
