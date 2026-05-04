package com.actmira.evaluator;

import java.util.List;
import java.util.ArrayList;

public class MiraArray implements MiraObject {
    private final List<MiraObject> elements;

    public MiraArray(List<MiraObject> elements) {
        this.elements = elements;
    }

    public List<MiraObject> getElements() { return elements; }

    @Override
    public String type() { return "ARRAY"; }

    @Override
    public String inspect() {
        StringBuilder out = new StringBuilder();
        out.append("[");
        List<String> elems = new ArrayList<>();
        for (MiraObject e : elements) {
            elems.add(e.inspect());
        }
        out.append(String.join(", ", elems));
        out.append("]");
        return out.toString();
    }
}
