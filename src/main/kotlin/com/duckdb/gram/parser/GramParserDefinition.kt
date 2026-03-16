package com.duckdb.gram.parser

import com.duckdb.gram.GramFile
import com.duckdb.gram.GramLanguage
import com.duckdb.gram.lexer.GramLexer
import com.duckdb.gram.lexer.GramTokenTypes
import com.duckdb.gram.psi.GramPsiFactory
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class GramParserDefinition : ParserDefinition {
    companion object {
        @JvmField
        val FILE = IFileElementType(GramLanguage)
    }

    override fun createLexer(project: Project): Lexer = GramLexer()
    override fun createParser(project: Project): PsiParser = GramParser()
    override fun getFileNodeType(): IFileElementType = FILE
    override fun getWhitespaceTokens(): TokenSet = GramTokenTypes.WHITESPACE_SET
    override fun getCommentTokens(): TokenSet = GramTokenTypes.COMMENT_SET
    override fun getStringLiteralElements(): TokenSet = GramTokenTypes.STRING_SET

    override fun createElement(node: ASTNode): PsiElement = GramPsiFactory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = GramFile(viewProvider)
}
