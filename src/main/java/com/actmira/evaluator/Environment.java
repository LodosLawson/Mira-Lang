package com.actmira.evaluator;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, MiraObject> store = new HashMap<>();
    private final Environment outer;

    public Environment(Environment outer) {
        this.outer = outer;
    }

    public Environment() {
        this.outer = null;
    }

    public MiraObject get(String name) {
        MiraObject obj = store.get(name);
        if (obj == null && outer != null) {
            obj = outer.get(name);
        }
        return obj;
    }

    public MiraObject set(String name, MiraObject val) {
        store.put(name, val);
        return val;
    }
}
