package com.sobrenaturaldirector.capability;

import com.sobrenaturaldirector.content.model.SemanticCapability;

public final class CapabilityVocabulary {
    public static final SemanticCapability BLOCK_MUTATION = new SemanticCapability("director:block_mutation");
    public static final SemanticCapability ROLLBACK_SAFE = new SemanticCapability("director:rollback_safe");
    public static final SemanticCapability STRUCTURE_SOURCE = new SemanticCapability("director:structure_source");
    public static final SemanticCapability ACTOR_SOURCE = new SemanticCapability("director:actor_source");
    public static final SemanticCapability THREAT_SOURCE = new SemanticCapability("director:threat_source");
    public static final SemanticCapability NATURAL_THREAT_PRESSURE_CONTROL = new SemanticCapability("director:natural_threat_pressure_control");
    // Phase 2 semantic foundation. These are contracts, not concrete provider claims.
    public static final SemanticCapability BLOCK_SOURCE = new SemanticCapability("director:block_source");
    public static final SemanticCapability ITEM_SOURCE = new SemanticCapability("director:item_source");
    public static final SemanticCapability DIMENSION_SOURCE = new SemanticCapability("director:dimension_source");
    public static final SemanticCapability ENVIRONMENT_SOURCE = new SemanticCapability("director:environment_source");
    public static final SemanticCapability ENTITY_ATTRIBUTE_CONTROL = new SemanticCapability("director:entity_attribute_control");
    public static final SemanticCapability ENTITY_MOVEMENT_CONTROL = new SemanticCapability("director:entity_movement_control");
    public static final SemanticCapability ENTITY_BEHAVIOR_CONTROL = new SemanticCapability("director:entity_behavior_control");
    public static final SemanticCapability ENTITY_TARGET_CONTROL = new SemanticCapability("director:entity_target_control");
    public static final SemanticCapability ENTITY_LIFECYCLE_CONTROL = new SemanticCapability("director:entity_lifecycle_control");
    public static final SemanticCapability STRUCTURE_COMPOSITION = new SemanticCapability("director:structure_composition");
    public static final SemanticCapability ITEM_GIVE = new SemanticCapability("director:item_give");
    public static final SemanticCapability ITEM_DROP = new SemanticCapability("director:item_drop");
    public static final SemanticCapability ITEM_REMOVE = new SemanticCapability("director:item_remove");
    public static final SemanticCapability EQUIPMENT_CONTROL = new SemanticCapability("director:equipment_control");
    public static final SemanticCapability ENVIRONMENT_MUTATION = new SemanticCapability("director:environment_mutation");
    public static final SemanticCapability DIMENSION_ACTION = new SemanticCapability("director:dimension_action");
    private CapabilityVocabulary() { }
}
