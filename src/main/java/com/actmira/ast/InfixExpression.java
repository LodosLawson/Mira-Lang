package com.actmira.ast;

import com.actmira.lexer.Token;

public class InfixExpression implements Expression {
    private final Token token; // The operator token, e.g. +
    private final Expression left;
    private final String operator;
    private Expression right;

    public InfixExpression(Token token, String operator, Expression left) {
        this.token = token;
        this.operator = operator;
        this.left = left;
    }

    public void setRight(Expression right) { this.right = right; }
    public Expression getLeft() { return left; }
    public Expression getRight() { return right; }
    public String getOperator() { return operator; }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() {
        return "(" + left.string() + " " + operator + " " + right.string() + ")";
    }
}
