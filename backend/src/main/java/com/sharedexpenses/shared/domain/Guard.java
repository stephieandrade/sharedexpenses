package com.sharedexpenses.shared.domain;

public final class Guard {

    private Guard() {
    }

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new BusinessRuleException(message);
        }
    }

    public static <T> T required(T value, String message) {
        check(value != null, message);
        return value;
    }

    public static String requiredText(String value, String message) {
        check(value != null && !value.isBlank(), message);
        return value.strip();
    }
}
