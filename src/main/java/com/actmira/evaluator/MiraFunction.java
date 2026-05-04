package com.actmira.evaluator;

import com.actmira.ast.BlockStatement;
import com.actmira.ast.Identifier;
import java.util.List;

public class MiraFunction implements MiraObject {
    private final List<Identifier> parameters;
    private final BlockStatement body;
    private final Environment env;

    public MiraFunction(List<Identifier> parameters, BlockStatement body, Environment env) {
        this.parameters = parameters;
        this.body = body;
        this.env = env;
    }

    public List<Identifier> getParameters() { return parameters; }
    public BlockStatement getBody() { return body; }
    public Environment getEnv() { return env; }

    @Override
    public String type() { return "FUNCTION"; }

    @Override
    public String inspect() { return "fn() { ... }"; }
}
