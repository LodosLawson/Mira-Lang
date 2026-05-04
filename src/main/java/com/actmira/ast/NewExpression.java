package com.actmira.ast;

import com.actmira.lexer.Token;

public class NewExpression implements Expression {
    private final Token token; // The 'new' token
    private Identifier className;
    
    public NewExpression(Token token) {
        this.token = token;
    }

    public Identifier getClassName() { return className; }
    public void setClassName(Identifier className) { this.className = className; }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() {
        return "new " + className.string() + "()";
    }
}
