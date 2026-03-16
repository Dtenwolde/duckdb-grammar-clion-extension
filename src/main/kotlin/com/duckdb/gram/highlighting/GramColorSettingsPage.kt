package com.duckdb.gram.highlighting

import com.duckdb.gram.icons.GramIcons
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class GramColorSettingsPage : ColorSettingsPage {
    private val descriptors = arrayOf(
        AttributesDescriptor("Rule name", GramSyntaxHighlighter.RULE_NAME),
        AttributesDescriptor("Separator (<-)", GramSyntaxHighlighter.SEPARATOR),
        AttributesDescriptor("Literal ('...')", GramSyntaxHighlighter.LITERAL),
        AttributesDescriptor("Reference", GramSyntaxHighlighter.REFERENCE),
        AttributesDescriptor("Operator (parentheses, negation)", GramSyntaxHighlighter.OPERATOR),
        AttributesDescriptor("Choice (/)", GramSyntaxHighlighter.CHOICE),
        AttributesDescriptor("Quantifier (? * +)", GramSyntaxHighlighter.QUANTIFIER),
        AttributesDescriptor("Comment", GramSyntaxHighlighter.COMMENT),
        AttributesDescriptor("Regex ([...] or <...>)", GramSyntaxHighlighter.REGEX),
        AttributesDescriptor("Parameter", GramSyntaxHighlighter.PARAMETER),
    )

    override fun getIcon(): Icon = GramIcons.FILE
    override fun getHighlighter(): SyntaxHighlighter = GramSyntaxHighlighter()
    override fun getDemoText(): String = """
        # DuckDB PEG Grammar
        AttachStatement <- 'ATTACH' OrReplace? IfNotExists? Database? DatabasePath AttachAlias? AttachOptions?

        Database <- 'DATABASE'
        DatabasePath <- StringLiteral
        AttachAlias <- 'AS' ColId
        AttachOptions <- GenericCopyOptionList

        List(D) <- D (',' D)*

        %whitespace <- [ \t\r\n]*
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = descriptors
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDisplayName(): String = "DuckDB Grammar"
}
