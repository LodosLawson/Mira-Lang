package com.actmira.ast;

import com.actmira.lexer.Token;
import java.util.ArrayList;
import java.util.List;

public class FunctionLiteral implements Expression {
    private final Token token; // The 'fn' token
    private Identifier name;
    private final List<Identifier> parameters = new ArrayList<>();
    private BlockStatement body;

    public FunctionLiteral(Token token) {
        this.token = token;
    }

    public Identifier getName() { return name; }
    public void setName(Identifier name) { this.name = name; }
    
    public List<Identifier> getParameters() { return parameters; }
    
    public BlockStatement getBody() { return body; }
    public void setBody(BlockStatement body) { this.body = body; }

    @Override
    public void expressionNode() {}

    @Override
    public String tokenLiteral() { return token.literal(); }

    @Override
    public String string() {
        StringBuilder out = new StringBuilder();
        out.append(tokenLiteral()).append(" ");
        if (name != null) {
            out.append(name.string());
        }
        out.append("(");
        List<String> params = new ArrayList<>();
        for (Identifier p : parameters) {
            params.add(p.string());
        }
        out.append(String.join(", ", params));
        out.append(") ");
        if (body != null) {
            out.append(body.string());
        }
        return out.toString();
    }
}
