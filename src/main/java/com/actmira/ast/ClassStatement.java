package com.actmira.ast;

import com.actmira.lexer.Token;
import java.util.ArrayList;
import java.util.List;

public class ClassStatement implements Statement {
    private final Token token; // The 'class' token
    private Identifier name;
    private final List<FunctionLiteral> methods = new ArrayList<>();

    public ClassStatement(Token token) {
        this.token = token;
    }

    public Identifier getName() { return name; }
    public void setName(Identifier name) { this.name = name; }
    
    public List<FunctionLiteral> getMethods() { return methods; }

    @Override
    public void statementNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        out.append(tokenLiteral()).append(" ");
        if (name != null) {
            out.append(name.string());
        }
        out.append(" {\n");
        for (FunctionLiteral method : methods) {
            out.append("  ").append(method.string()).append("\n");
        }
        out.append("}");
        return out.toString();
    }
}
