package com.actmira.ast;

import com.actmira.lexer.Token;

public class ExpressionStatement implements Statement {
    private final Token token; // the first token of the expression
    private Expression expression;

    public ExpressionStatement(Token token) {
        this.token = token;
    }

    public Expression getExpression() { return expression; }
    public void setExpression(Expression expression) { this.expression = expression; }

    @Override
    public void statementNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() {
        if (expression != null) {
            return expression.string();
        }
        return "";
    }
}
