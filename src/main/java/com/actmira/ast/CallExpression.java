package com.actmira.ast;

import com.actmira.lexer.Token;
import java.util.ArrayList;
import java.util.List;

public class CallExpression implements Expression {
    private final Token token; // The '(' token
    private Expression function; // Identifier or FunctionLiteral
    private final List<Expression> arguments = new ArrayList<>();

    public CallExpression(Token token, Expression function) {
        this.token = token;
        this.function = function;
    }

    public Expression getFunction() { return function; }
    public List<Expression> getArguments() { return arguments; }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        out.append(function.string()).append("(");
        List<String> args = new ArrayList<>();
        for (Expression a : arguments) {
            args.add(a.string());
        }
        out.append(String.join(", ", args));
        out.append(")");
        return out.toString();
    }
}
