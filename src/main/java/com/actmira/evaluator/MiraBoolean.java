package com.actmira.evaluator;

public class MiraBoolean implements MiraObject {
    private final boolean value;

    public MiraBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() { return value; }

    @Override
    public String type() { return "BOOLEAN"; }

    @Override
    public String inspect() { return String.valueOf(value); }
}
