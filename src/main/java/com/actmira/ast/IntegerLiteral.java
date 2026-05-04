package com.actmira.ast;

import com.actmira.lexer.Token;

public class IntegerLiteral implements Expression {
    private final Token token;
    private final int value;

    public IntegerLiteral(Token token, int value) {
        this.token = token;
        this.value = value;
    }

    public int getValue() { return value; }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() { return token.literal(); }
}
