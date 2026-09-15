package com.sobrenaturaldirector.execution.adapter;

import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.execution.*;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.provider.DirectorContentProvider;
import com.sobrenaturaldirector.situation.SemanticRole;
import com.sobrenaturaldirector.situation.SituationContext;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.threat.*;

/** Production threat adapter reusing the validated plan-to-execution bridge. */
public final class SlenderManExecutionSlicePreparer implements ExecutionSlicePreparer {
    private final DirectorProviderRegistry registry;
    public SlenderManExecutionSlicePreparer(DirectorProviderRegistry registry) { if (registry == null) throw new IllegalArgumentException("registry is required"); this.registry = registry; }
    public boolean supports(CapabilityDescriptor capability) { return capability != null && CapabilityVocabulary.THREAT_SOURCE.equals(capability.getId()) && new ProviderId("slenderman").equals(capability.getProvider()); }
    public ControlledProviderSlice prepare(final CandidatePlan plan, CapabilityDescriptor capability, final ExecutionPreparationContext context) {
        final String id = context.getExecutionId() + ":threat";
        final PlanExecutionSlice slice = new PlanExecutionSlice(plan, context.getBlueprint(), SemanticRole.THREAT);
        final PlanExecutionAuthorization authorization = new PlanExecutionAuthorization(context.getExecutionId(), plan.signature(), context.getBlueprint().fingerprint(), SemanticRole.THREAT, CapabilityVocabulary.THREAT_SOURCE.getValue(), capability.getProvider(), true);
        final ControlledThreatPlanExecutionBridge bridge = new ControlledThreatPlanExecutionBridge(registry);
        return new ControlledProviderSlice() {
            public String getSliceId() { return id; }
            public CoordinatedSlicePhase getPhase() { return CoordinatedSlicePhase.THREAT; }
            public ProviderId getProviderId() { return capabilityProvider(); }
            public CoordinatedSliceResult preflight() { DirectorContentProvider provider = registry.get(capabilityProvider()); return provider != null && provider.isAvailable() && context.getSituation() != null && context.getBlueprint() != null ? ok("threat preflight") : fail("threat provider unavailable"); }
            public CoordinatedSliceResult execute() { ThreatExecutionResult result = bridge.execute(slice, authorization, context.getSituation(), context.getMemory(), context.getWorld(), context.getSaved(), true); System.out.println("[MPE-THREAT-ADAPTER] status=" + result.getStatus() + " requestId=" + result.getRequestId()); return result.getStatus() == ThreatExecutionResult.Status.CREATED || result.getStatus() == ThreatExecutionResult.Status.RECONCILED_EXISTING ? ok(result.getStatus().name()) : fail(result.getStatus().name()); }
            public CoordinatedSliceResult reconcile() { ThreatExecutionResult result = bridge.execute(slice, authorization, context.getSituation(), context.getMemory(), context.getWorld(), context.getSaved(), true); return result.getStatus() == ThreatExecutionResult.Status.RECONCILED_EXISTING || result.getStatus() == ThreatExecutionResult.Status.CREATED ? ok("reconciled") : fail(result.getStatus().name()); }
            public CoordinatedSliceResult compensate() { ThreatExecutionResult result = bridge.cleanup(slice, authorization, context.getWorld(), context.getSaved(), true); return result.getStatus() == ThreatExecutionResult.Status.CLEANUP_EXECUTED ? CoordinatedSliceResult.of(CoordinatedSliceResult.Status.COMPENSATED, "threat removed") : fail(result.getStatus().name()); }
            private ProviderId capabilityProvider() { return capability.getProvider(); }
        };
    }
    private static CoordinatedSliceResult ok(String detail) { return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.EXECUTED, detail); }
    private static CoordinatedSliceResult fail(String detail) { return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.FAILED, detail); }
}
