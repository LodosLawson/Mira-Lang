package com.actmira.ast;

import com.actmira.lexer.Token;

public class IndexExpression implements Expression {
    private final Token token; // The [ token
    private final Expression left;
    private Expression index;

    public IndexExpression(Token token, Expression left) {
        this.token = token;
        this.left = left;
    }

    public void setIndex(Expression index) {
        this.index = index;
    }

    public Expression getLeft() { return left; }
    public Expression getIndex() { return index; }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() {
        return "(" + left.string() + "[" + index.string() + "])";
    }
}
