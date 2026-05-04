package com.actmira.evaluator;

public class MiraNull implements MiraObject {

    @Override
    public String type() { return "NULL"; }

    @Override
    public String inspect() { return "null"; }
}
