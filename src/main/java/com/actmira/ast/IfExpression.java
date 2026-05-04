package com.actmira.ast;

import com.actmira.lexer.Token;

public class IfExpression implements Expression {
    private final Token token; // The 'if' token
    private Expression condition;
    private BlockStatement consequence;
    private BlockStatement alternative;

    public IfExpression(Token token) {
        this.token = token;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public void setConsequence(BlockStatement consequence) {
        this.consequence = consequence;
    }

    public void setAlternative(BlockStatement alternative) {
        this.alternative = alternative;
    }

    public Expression getCondition() { return condition; }
    public BlockStatement getConsequence() { return consequence; }
    public BlockStatement getAlternative() { return alternative; }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() {
        StringBuilder sb = new StringBuilder();
        sb.append("if").append(condition.string()).append(" ").append(consequence.string());
        if (alternative != null) {
            sb.append("else ").append(alternative.string());
        }
        return sb.toString();
    }
}
