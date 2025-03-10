package com.kami.utils;

public enum ConfigEnumTypes {
    BEST_TRADES("best-trades"),
    WORST_TRADES("worst-trades"),
    SHARED_TRADES("shared-trades"),
    ONE_TIME_TRADES("one-time-trades");

    private final String value;

    ConfigEnumTypes(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }
}
