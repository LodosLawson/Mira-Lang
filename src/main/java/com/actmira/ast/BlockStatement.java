package com.actmira.ast;

import com.actmira.lexer.Token;
import java.util.ArrayList;
import java.util.List;

public class BlockStatement implements Statement {
    private final Token token; // the { token
    private final List<Statement> statements = new ArrayList<>();

    public BlockStatement(Token token) {
        this.token = token;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public void statementNode() {}

    @Override
    public String tokenLiteral() {
        return token.literal();
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
