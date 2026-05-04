package com.actmira.evaluator;

public class MiraString implements MiraObject {
    private final String value;

    public MiraString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String type() {
        return "STRING";
    }

    @Override
    public String inspect() {
        return value;
    }
}
