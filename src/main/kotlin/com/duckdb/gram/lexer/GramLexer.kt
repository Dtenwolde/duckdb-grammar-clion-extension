package com.duckdb.gram.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

/**
 * Hand-written stateful lexer replicating peg_parser.cpp's three-state machine.
 *
 * States:
 *   RULE_NAME       — looking for a rule name (start of a rule)
 *   RULE_SEPARATOR  — looking for optional parameter + '<-'
 *   RULE_DEFINITION — parsing the rule body
 *
 * State is encoded in a single Int to support restart:
 *   bits 0-1: parse state (0=RULE_NAME, 1=RULE_SEPARATOR, 2=RULE_DEFINITION)
 *   bits 2-7: bracket count (max 63 — grammar never nests that deep)
 *   bit  8:   inOrClause flag
 *   bit  9:   ruleBodyEmpty flag (set after '<-', cleared on first body token)
 */
class GramLexer : LexerBase() {

    private companion object {
        const val S_RULE_NAME       = 0
        const val S_RULE_SEPARATOR  = 1
        const val S_RULE_DEFINITION = 2

        fun encodeState(state: Int, bracketCount: Int, inOrClause: Boolean, ruleBodyEmpty: Boolean = false): Int =
            (state and 0x3) or
            ((bracketCount and 0x3F) shl 2) or
            (if (inOrClause) 0x100 else 0) or
            (if (ruleBodyEmpty) 0x200 else 0)

        fun decodeState(packed: Int)          = packed and 0x3
        fun decodeBracketCount(packed: Int)   = (packed shr 2) and 0x3F
        fun decodeInOrClause(packed: Int)     = (packed and 0x100) != 0
        fun decodeRuleBodyEmpty(packed: Int)  = (packed and 0x200) != 0
    }

    private var buffer: CharSequence = ""
    private var bufferEnd: Int = 0
    private var tokenStart: Int = 0
    private var tokenEnd: Int = 0
    private var tokenType: IElementType? = null

    // packed lexer state (for restart support)
    private var packedState: Int = encodeState(S_RULE_NAME, 0, false)

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.bufferEnd = endOffset
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        this.tokenType = null
        this.packedState = initialState
        advance()
    }

    override fun getState(): Int = packedState

    override fun getTokenType(): IElementType? = tokenType

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = bufferEnd

    override fun advance() {
        tokenStart = tokenEnd
        if (tokenStart >= bufferEnd) {
            tokenType = null
            return
        }
        scanToken()
    }

    private fun ch(pos: Int) = if (pos < bufferEnd) buffer[pos] else '\u0000'

    private fun isAlphaNumeric(c: Char) = c.isLetterOrDigit() || c == '_'

    private fun scanToken() {
        var pos = tokenStart
        var parseState    = decodeState(packedState)
        var bracketCount  = decodeBracketCount(packedState)
        var inOrClause    = decodeInOrClause(packedState)
        var ruleBodyEmpty = decodeRuleBodyEmpty(packedState)

        val c = ch(pos)

        // Comments are valid in any state
        if (c == '#') {
            while (pos < bufferEnd && ch(pos) != '\n' && ch(pos) != '\r') pos++
            tokenEnd = pos
            tokenType = GramTokenTypes.COMMENT
            packedState = encodeState(parseState, bracketCount, inOrClause, ruleBodyEmpty)
            return
        }

        // Handle newlines as rule terminators in RULE_DEFINITION
        if (parseState == S_RULE_DEFINITION && (c == '\n' || c == '\r')) {
            if (bracketCount == 0 && !inOrClause) {
                if (ruleBodyEmpty) {
                    // No body content yet (newline immediately after '<-') — stay in RULE_DEFINITION.
                    // Consume all following whitespace so the body can start on the next line.
                    while (pos < bufferEnd && (ch(pos) == '\n' || ch(pos) == '\r' || ch(pos) == ' ' || ch(pos) == '\t')) pos++
                    tokenEnd = pos
                    tokenType = GramTokenTypes.WHITE_SPACE
                    packedState = encodeState(S_RULE_DEFINITION, 0, false, ruleBodyEmpty = true)
                } else {
                    // Normal rule termination — consume the newline(s) and reset to RULE_NAME.
                    while (pos < bufferEnd && (ch(pos) == '\n' || ch(pos) == '\r')) pos++
                    tokenEnd = pos
                    tokenType = GramTokenTypes.WHITE_SPACE
                    packedState = encodeState(S_RULE_NAME, 0, false)
                }
                return
            } else {
                // multi-line rule continuation — treat as whitespace, keep state
                while (pos < bufferEnd && (ch(pos) == '\n' || ch(pos) == '\r' || ch(pos) == ' ' || ch(pos) == '\t')) pos++
                tokenEnd = pos
                tokenType = GramTokenTypes.WHITE_SPACE
                // After a newline in or-clause, the or-clause persists until next real token
                packedState = encodeState(parseState, bracketCount, inOrClause, ruleBodyEmpty)
                return
            }
        }

        // Whitespace
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
            while (pos < bufferEnd && (ch(pos) == ' ' || ch(pos) == '\t' || ch(pos) == '\n' || ch(pos) == '\r')) pos++
            tokenEnd = pos
            tokenType = GramTokenTypes.WHITE_SPACE
            packedState = encodeState(parseState, bracketCount, inOrClause, ruleBodyEmpty)
            return
        }

        when (parseState) {
            S_RULE_NAME -> {
                if (c == '/') {
                    // Leading '/' on a continuation line (alternative on a new line).
                    // Treat it as a choice operator and re-enter RULE_DEFINITION.
                    pos++
                    tokenEnd = pos
                    tokenType = GramTokenTypes.CHOICE
                    packedState = encodeState(S_RULE_DEFINITION, 0, true)
                } else {
                    if (ch(pos) == '%') pos++
                    while (pos < bufferEnd && isAlphaNumeric(ch(pos))) pos++
                    tokenEnd = pos
                    tokenType = GramTokenTypes.RULE_NAME
                    packedState = encodeState(S_RULE_SEPARATOR, 0, false)
                }
            }

            S_RULE_SEPARATOR -> {
                if (c == '(') {
                    // consume '('
                    pos++
                    // emit the opening paren as OPERATOR
                    tokenEnd = pos
                    tokenType = GramTokenTypes.OPERATOR
                    packedState = encodeState(S_RULE_SEPARATOR, 0, false)
                    // Next advance will emit the parameter name, then ')'
                    // We need a sub-state; we use bracketCount==1 to mean "inside param parens"
                    // Actually: let's scan the param now as part of this token
                    // Rewind: emit '(' as OPERATOR first
                    // (already done above)
                    // But wait — we need to be back in S_RULE_SEPARATOR to scan parameter.
                    // We set a special marker: bracketCount=1 means "next identifier is PARAMETER"
                    packedState = encodeState(S_RULE_SEPARATOR, 1, false)
                } else if (bracketCount == 1 && isAlphaNumeric(c)) {
                    // emit parameter name
                    val start = pos
                    while (pos < bufferEnd && isAlphaNumeric(ch(pos))) pos++
                    tokenEnd = pos
                    tokenType = GramTokenTypes.PARAMETER
                    packedState = encodeState(S_RULE_SEPARATOR, 2, false)
                } else if (bracketCount == 2 && c == ')') {
                    pos++
                    tokenEnd = pos
                    tokenType = GramTokenTypes.OPERATOR
                    packedState = encodeState(S_RULE_SEPARATOR, 0, false)
                } else if (c == '<' && ch(pos + 1) == '-') {
                    pos += 2
                    tokenEnd = pos
                    tokenType = GramTokenTypes.SEPARATOR
                    // Mark ruleBodyEmpty=true — body hasn't started yet
                    packedState = encodeState(S_RULE_DEFINITION, 0, false, ruleBodyEmpty = true)
                } else {
                    pos++
                    tokenEnd = pos
                    tokenType = GramTokenTypes.BAD_CHAR
                    packedState = encodeState(parseState, bracketCount, inOrClause)
                }
            }

            S_RULE_DEFINITION -> {
                inOrClause    = false  // will be set explicitly if needed below
                ruleBodyEmpty = false  // first real token clears the empty-body flag

                when {
                    c == '\'' -> {
                        pos++ // skip opening quote
                        while (pos < bufferEnd && ch(pos) != '\'') {
                            if (ch(pos) == '\\') pos++ // escape
                            pos++
                        }
                        if (pos < bufferEnd) pos++ // skip closing quote
                        tokenEnd = pos
                        tokenType = GramTokenTypes.LITERAL
                        packedState = encodeState(S_RULE_DEFINITION, bracketCount, false)
                    }

                    c == '[' || c == '<' -> {
                        val closingChar = if (c == '[') ']' else '>'
                        // Disambiguate '<' from '<-' (separator) — shouldn't happen in RULE_DEFINITION
                        // but handle gracefully
                        pos++
                        while (pos < bufferEnd && ch(pos) != closingChar) {
                            if (ch(pos) == '\\') pos++
                            pos++
                        }
                        if (pos < bufferEnd) pos++
                        tokenEnd = pos
                        tokenType = GramTokenTypes.REGEX
                        packedState = encodeState(S_RULE_DEFINITION, bracketCount, false)
                    }

                    isAlphaNumeric(c) -> {
                        val start = pos
                        while (pos < bufferEnd && isAlphaNumeric(ch(pos))) pos++
                        if (ch(pos) == '(') {
                            // function call: emit the identifier as REFERENCE, then '(' next advance
                            tokenEnd = pos
                            tokenType = GramTokenTypes.REFERENCE
                            // bump bracket count and mark that next '(' needs emitting
                            packedState = encodeState(S_RULE_DEFINITION, bracketCount, false)
                            // The '(' will be emitted naturally as OPERATOR in next advance
                        } else {
                            tokenEnd = pos
                            tokenType = GramTokenTypes.REFERENCE
                            packedState = encodeState(S_RULE_DEFINITION, bracketCount, false)
                        }
                    }

                    c == '(' -> {
                        bracketCount++
                        pos++
                        tokenEnd = pos
                        tokenType = GramTokenTypes.OPERATOR
                        packedState = encodeState(S_RULE_DEFINITION, bracketCount, false)
                    }

                    c == ')' -> {
                        if (bracketCount > 0) bracketCount--
                        pos++
                        tokenEnd = pos
                        tokenType = GramTokenTypes.OPERATOR
                        packedState = encodeState(S_RULE_DEFINITION, bracketCount, false)
                    }

                    c == '/' -> {
                        pos++
                        tokenEnd = pos
                        tokenType = GramTokenTypes.CHOICE
                        // '/' sets inOrClause — rule continues on next line
                        packedState = encodeState(S_RULE_DEFINITION, bracketCount, true)
                    }

                    c == '?' || c == '*' || c == '+' -> {
                        pos++
                        tokenEnd = pos
                        tokenType = GramTokenTypes.QUANTIFIER
                        packedState = encodeState(S_RULE_DEFINITION, bracketCount, false)
                    }

                    c == '!' -> {
                        pos++
                        tokenEnd = pos
                        tokenType = GramTokenTypes.OPERATOR
                        packedState = encodeState(S_RULE_DEFINITION, bracketCount, false)
                    }

                    else -> {
                        pos++
                        tokenEnd = pos
                        tokenType = GramTokenTypes.BAD_CHAR
                        packedState = encodeState(S_RULE_DEFINITION, bracketCount, false)
                    }
                }
            }

            else -> {
                pos++
                tokenEnd = pos
                tokenType = GramTokenTypes.BAD_CHAR
                packedState = encodeState(parseState, bracketCount, inOrClause)
            }
        }
    }
}
