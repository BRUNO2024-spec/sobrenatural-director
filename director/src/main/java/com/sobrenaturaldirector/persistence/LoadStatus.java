package com.sobrenaturaldirector.persistence;

public enum LoadStatus {
    FRESH,
    LOADED,
    UNSUPPORTED_OLD_SCHEMA,
    UNSUPPORTED_FUTURE_SCHEMA,
    MISSING_SCHEMA,
    CORRUPT
}
