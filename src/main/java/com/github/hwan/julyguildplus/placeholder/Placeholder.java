package com.github.hwan.julyguildplus.placeholder;

public class Placeholder {
    private String key;
    private Object value;

    Placeholder(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
