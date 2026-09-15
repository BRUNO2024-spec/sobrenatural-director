package com.sobrenaturaldirector.threat;

import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.situation.SemanticRole;
import com.sobrenaturaldirector.situation.SituationBlueprint;

/** A deliberately narrow, role-scoped execution view of a larger semantic plan. */
public final class PlanExecutionSlice {
    private final CandidatePlan plan;
    private final SituationBlueprint blueprint;
    private final SemanticRole role;
    private final CapabilityDescriptor capability;

    public PlanExecutionSlice(CandidatePlan plan, SituationBlueprint blueprint, SemanticRole role) {
        if (plan == null || blueprint == null || role == null) throw new IllegalArgumentException("plan, blueprint and role are required");
        if (role != SemanticRole.THREAT) throw new IllegalArgumentException("only threat slice is enabled");
        CapabilityDescriptor selected = plan.getSelections().get(CapabilityVocabulary.THREAT_SOURCE);
        if (selected == null || (!blueprint.getRequiredRoles().contains(role) && !blueprint.getOptionalRoles().contains(role))) throw new IllegalArgumentException("threat capability is not selected by blueprint");
        this.plan = plan; this.blueprint = blueprint; this.role = role; this.capability = selected;
    }
    public CandidatePlan getPlan() { return plan; }
    public SituationBlueprint getBlueprint() { return blueprint; }
    public SemanticRole getRole() { return role; }
    public CapabilityDescriptor getCapability() { return capability; }
    public String getParentPlanId() { return plan.signature(); }
    public String getPlanFingerprint() { return blueprint.fingerprint(); }
    public ProviderId getProviderId() { return capability.getProvider(); }
}
