package com.actmira.ast;

import com.actmira.lexer.Token;

public class LetStatement implements Statement {
    private final Token token; // the TokenType.LET token
    private Identifier name;
    private Expression value;

    public LetStatement(Token token) {
        this.token = token;
    }

    public Identifier getName() {
        return name;
    }

    public void setName(Identifier name) {
        this.name = name;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    @Override
    public void statementNode() {}

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        out.append(tokenLiteral()).append(" ");
        if (name != null) {
            out.append(name.string());
        }
        out.append(" = ");
        if (value != null) {
            out.append(value.string());
        }
        out.append(";");
        return out.toString();
    }
}
