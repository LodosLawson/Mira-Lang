package com.actmira.ast;

import com.actmira.lexer.Token;

public class ReturnStatement implements Statement {
    private final Token token; // the 'return' token
    private Expression returnValue;

    public ReturnStatement(Token token) {
        this.token = token;
    }

    public Expression getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Expression returnValue) {
        this.returnValue = returnValue;
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
        if (returnValue != null) {
            out.append(returnValue.string());
        }
        out.append(";");
        return out.toString();
    }
}
