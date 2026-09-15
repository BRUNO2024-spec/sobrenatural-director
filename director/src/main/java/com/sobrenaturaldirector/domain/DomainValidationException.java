package com.sobrenaturaldirector.domain;

public final class DomainValidationException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    public DomainValidationException(String message) { super(message); }
}
