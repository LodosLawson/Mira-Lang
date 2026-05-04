package com.actmira.ast;

import com.actmira.lexer.Token;
import java.util.ArrayList;
import java.util.List;

public class ArrayLiteral implements Expression {
    private final Token token; // the '[' token
    private final List<Expression> elements = new ArrayList<>();

    public ArrayLiteral(Token token) {
        this.token = token;
    }

    public List<Expression> getElements() {
        return elements;
    }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        out.append("[");
        List<String> elems = new ArrayList<>();
        for (Expression e : elements) {
            elems.add(e.string());
        }
        out.append(String.join(", ", elems));
        out.append("]");
        return out.toString();
    }
}
