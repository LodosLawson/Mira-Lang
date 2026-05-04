package com.actmira.evaluator;

public class MiraError implements MiraObject {
    private final String message;

    public MiraError(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }

    @Override
    public String type() { return "ERROR"; }

    @Override
    public String inspect() { return "ERROR: " + message; }
}
