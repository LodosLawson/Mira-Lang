package com.actmira.ast;

import com.actmira.lexer.Token;

public class BooleanLiteral implements Expression {
    private final Token token;
    private final boolean value;

    public BooleanLiteral(Token token, boolean value) {
        this.token = token;
        this.value = value;
    }

    public boolean getValue() { return value; }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() { return String.valueOf(value); }
}
