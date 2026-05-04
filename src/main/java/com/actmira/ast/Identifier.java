package com.actmira.ast;

import com.actmira.lexer.Token;

public class Identifier implements Expression {
    private final Token token; // the TokenType.IDENT token
    private final String value;

    public Identifier(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return value;
    }
}
