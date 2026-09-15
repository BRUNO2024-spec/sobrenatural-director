package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.sobrenaturaldirector.capability.CapabilityQuery;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.content.model.SemanticCapability;
import com.sobrenaturaldirector.decision.model.ActionSafety;
import com.sobrenaturaldirector.decision.model.CandidateAction;
import com.sobrenaturaldirector.decision.model.Intent;
import com.sobrenaturaldirector.environment.model.SemanticRegionProfile;
import com.sobrenaturaldirector.planning.environment.PlayerAreaSafetyPolicy;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.threat.ThreatLifecycleState;

/** Pure validation boundary. It only observes registry/profile state. */
public final class PersistentNarrativePlanValidator {
    private final PlayerAreaSafetyPolicy safety = new PlayerAreaSafetyPolicy();
    public NarrativePlanValidationResult validate(PersistentNarrativePlan plan, DirectorProviderRegistry registry, SemanticRegionProfile profile, long tick) {
        return validate(plan, registry, profile, tick, null);
    }
    public NarrativePlanValidationResult validate(PersistentNarrativePlan plan, DirectorProviderRegistry registry, SemanticRegionProfile profile, long tick, DirectorWorldSavedData data) {
        if (plan == null || registry == null) return result(NarrativePlanValidationResult.Status.INVALID, "INVALID_INPUT");
        if (plan.getState().isTerminal()) return result(NarrativePlanValidationResult.Status.TERMINAL, "TERMINAL_STATE:" + plan.getState());
        if (plan.getExpiryTick() >= 0 && tick > plan.getExpiryTick()) return result(NarrativePlanValidationResult.Status.EXPIRED, "PLAN_EXPIRED");
        List<String> reasons = new ArrayList<String>();
        for (Map.Entry<String,PersistentNarrativePlan.CapabilityRecord> entry : plan.getCapabilities().entrySet()) {
            SemanticCapability capability = new SemanticCapability(entry.getKey()); PersistentNarrativePlan.CapabilityRecord expected=entry.getValue(); boolean found=false;
            for (CapabilityDescriptor descriptor : registry.query(new CapabilityQuery(capability))) if (descriptor.getProvider().equals(new ProviderId(expected.getProvider())) && descriptor.getAvailability().atLeast(expected.getAvailability())) { found=true; break; }
            if (!found) reasons.add("CAPABILITY_UNAVAILABLE:" + entry.getKey() + "@" + expected.getProvider());
        }
        // A registry revision is only a cheap invalidation hint. Required semantic
        // capabilities above decide staleness; unrelated provider changes do not.
        ActionSafety actionSafety = plan.getImpact().name().equals("DESTRUCTIVE") ? ActionSafety.MUTATION : plan.getImpact().name().equals("HIGH_IMPACT") ? ActionSafety.MAJOR : plan.getImpact().name().equals("INTRUSIVE") ? ActionSafety.SPAWN : ActionSafety.NON_DESTRUCTIVE;
        java.util.Map<String,String> metadata=new java.util.LinkedHashMap<String,String>(); metadata.put("environmentRelation",plan.getRegionRelation());
        CandidateAction action=new CandidateAction("plan:"+plan.getBlueprintId(), Intent.valueOf(plan.getIntent()), "NARRATIVE", 0.0, null, metadata, null, actionSafety);
        if (!safety.assess(profile, action).isAllowed()) reasons.add("ENVIRONMENT_REJECTED");
        if (plan.isContinuation() && data != null && plan.getNarrativeId().length() > 0) {
            com.sobrenaturaldirector.threat.PersistentNarrativeThreat threat=data.getNarrativeThreat(plan.getNarrativeId());
            if (threat != null && (threat.getState() == ThreatLifecycleState.RESOLVED || threat.getState() == ThreatLifecycleState.FAILED)) reasons.add("NARRATIVE_NOT_ACTIVE:" + threat.getState());
        }
        return reasons.isEmpty() ? new NarrativePlanValidationResult(NarrativePlanValidationResult.Status.VALID, Collections.singletonList("PLAN_VALID")) : new NarrativePlanValidationResult(NarrativePlanValidationResult.Status.STALE, reasons);
    }
    private NarrativePlanValidationResult result(NarrativePlanValidationResult.Status status,String reason){return new NarrativePlanValidationResult(status,Collections.singletonList(reason));}
}
