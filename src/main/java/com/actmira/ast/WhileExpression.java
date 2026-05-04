package com.actmira.ast;

import com.actmira.lexer.Token;

public class WhileExpression implements Expression {
    private final Token token; // The 'while' token
    private Expression condition;
    private BlockStatement consequence;

    public WhileExpression(Token token) {
        this.token = token;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public void setConsequence(BlockStatement consequence) {
        this.consequence = consequence;
    }

    public Expression getCondition() { return condition; }
    public BlockStatement getConsequence() { return consequence; }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() {
        return "while" + condition.string() + " " + consequence.string();
    }
}
