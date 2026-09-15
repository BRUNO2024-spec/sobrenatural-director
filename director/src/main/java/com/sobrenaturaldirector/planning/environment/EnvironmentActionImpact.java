package com.sobrenaturaldirector.planning.environment;

import com.sobrenaturaldirector.decision.model.ActionSafety;
import com.sobrenaturaldirector.decision.model.CandidateAction;

public enum EnvironmentActionImpact {
    NON_INTRUSIVE, INTRUSIVE, DESTRUCTIVE, HIGH_IMPACT;
    public static EnvironmentActionImpact from(CandidateAction action) {
        String override = action.getMetadata().get("environmentImpact");
        if (override != null) try { return valueOf(override); } catch (IllegalArgumentException ignored) { }
        if (action.getSafety() == ActionSafety.MAJOR) return HIGH_IMPACT;
        if (action.getSafety() == ActionSafety.MUTATION) return DESTRUCTIVE;
        if (action.getSafety() == ActionSafety.SPAWN) return INTRUSIVE;
        return NON_INTRUSIVE;
    }
}
