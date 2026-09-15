package com.sobrenaturaldirector.capability;

public enum ProviderAvailability {
    DETECTED, DISCOVERED, SAFE_CAPABILITY, RUNTIME_CONTROL, COMPOSABLE;
    public boolean atLeast(ProviderAvailability other) { return ordinal() >= other.ordinal(); }
}
