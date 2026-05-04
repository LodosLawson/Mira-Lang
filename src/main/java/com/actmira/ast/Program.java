package com.actmira.ast;

import java.util.ArrayList;
import java.util.List;

public class Program implements Node {
    private final List<Statement> statements = new ArrayList<>();

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String tokenLiteral() {
        if (!statements.isEmpty()) {
            return statements.get(0).tokenLiteral();
        } else {
            return "";
        }
    }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        for (Statement stmt : statements) {
            out.append(stmt.string());
        }
        return out.toString();
    }
}
