package com.actmira.ast;

import com.actmira.lexer.Token;

public class PropertyAccess implements Expression {
    private final Token token; // The '.' token (assumes we add DOT to Lexer, or handles 'this.x')
    private Expression object; // e.g., 'this' or 'myObj'
    private Identifier property; // e.g., 'x'

    public PropertyAccess(Token token, Expression object, Identifier property) {
        this.token = token;
        this.object = object;
        this.property = property;
    }

    public Expression getObject() { return object; }
    public Identifier getProperty() { return property; }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() {
        return object.string() + "." + property.string();
    }
}
