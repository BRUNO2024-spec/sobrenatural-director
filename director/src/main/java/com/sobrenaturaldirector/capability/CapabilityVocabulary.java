package com.sobrenaturaldirector.capability;

import com.sobrenaturaldirector.content.model.SemanticCapability;

public final class CapabilityVocabulary {
    public static final SemanticCapability BLOCK_MUTATION = new SemanticCapability("director:block_mutation");
    public static final SemanticCapability ROLLBACK_SAFE = new SemanticCapability("director:rollback_safe");
    public static final SemanticCapability STRUCTURE_SOURCE = new SemanticCapability("director:structure_source");
    public static final SemanticCapability ACTOR_SOURCE = new SemanticCapability("director:actor_source");
    public static final SemanticCapability THREAT_SOURCE = new SemanticCapability("director:threat_source");
    public static final SemanticCapability NATURAL_THREAT_PRESSURE_CONTROL = new SemanticCapability("director:natural_threat_pressure_control");
    private CapabilityVocabulary() { }
}
