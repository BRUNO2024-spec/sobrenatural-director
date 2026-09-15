package com.sobrenaturaldirector.situation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable semantic composition description; it contains no provider identity. */
public final class SituationBlueprint {
    private final String id;
    private final SituationGoal goal;
    private final List<SemanticRole> requiredRoles, optionalRoles;
    private final SituationIntensity intensity;
    private final int baseScore, risk;
    public SituationBlueprint(String id, SituationGoal goal, List<SemanticRole> requiredRoles, List<SemanticRole> optionalRoles, SituationIntensity intensity, int baseScore, int risk) {
        if (id == null || goal == null || intensity == null || requiredRoles == null || optionalRoles == null) throw new IllegalArgumentException("invalid blueprint");
        this.id = id; this.goal = goal; this.requiredRoles = immutable(requiredRoles); this.optionalRoles = immutable(optionalRoles); this.intensity = intensity; this.baseScore = baseScore; this.risk = risk;
    }
    private static <T> List<T> immutable(List<T> values) { ArrayList<T> copy = new ArrayList<T>(values); for (T value : copy) if (value == null) throw new IllegalArgumentException("null blueprint element"); return Collections.unmodifiableList(copy); }
    public String getId() { return id; } public SituationGoal getGoal() { return goal; }
    public List<SemanticRole> getRequiredRoles() { return requiredRoles; } public List<SemanticRole> getOptionalRoles() { return optionalRoles; }
    public SituationIntensity getIntensity() { return intensity; } public int getBaseScore() { return baseScore; } public int getRisk() { return risk; }
    public String fingerprint() { return goal + "|" + requiredRoles + "|" + optionalRoles + "|" + intensity; }
}
