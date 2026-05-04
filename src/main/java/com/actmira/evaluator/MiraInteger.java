package com.actmira.evaluator;

public class MiraInteger implements MiraObject {
    private final int value;

    public MiraInteger(int value) {
        this.value = value;
    }

    public int getValue() { return value; }

    @Override
    public String type() { return "INTEGER"; }

    @Override
    public String inspect() { return String.valueOf(value); }
}
