package com.actmira.lexer;

import java.util.HashMap;
import java.util.Map;

public record Token(TokenType type, String literal) {

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("fn", TokenType.FUNCTION);
        keywords.put("let", TokenType.LET);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("return", TokenType.RETURN);
        keywords.put("Ustglobal", TokenType.USTGLOBAL);
        keywords.put("class", TokenType.CLASS);
        keywords.put("new", TokenType.NEW);
        keywords.put("this", TokenType.THIS);
        keywords.put("print", TokenType.PRINT);
        keywords.put("import", TokenType.IMPORT);
    }

    public static TokenType lookupIdent(String ident) {
        return keywords.getOrDefault(ident, TokenType.IDENT);
    }
}
