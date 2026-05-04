package com.actmira.evaluator;

public class MiraReturnValue implements MiraObject {
    private final MiraObject value;

    public MiraReturnValue(MiraObject value) {
        this.value = value;
    }

    public MiraObject getValue() { return value; }

    @Override
    public String type() { return "RETURN_VALUE"; }

    @Override
    public String inspect() { return value.inspect(); }
}
