package com.sobrenaturaldirector.domain;

public final class StableId {
    public static final int MAX_LENGTH = 128;

    private StableId() { }

    public static String require(String value, String field) {
        if (value == null || value.length() == 0 || value.length() > MAX_LENGTH) {
            throw new DomainValidationException(field + " must be 1.." + MAX_LENGTH + " characters");
        }
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                throw new DomainValidationException(field + " contains a control character");
            }
        }
        return value;
    }
}
