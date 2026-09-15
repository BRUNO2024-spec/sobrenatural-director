package com.sobrenaturaldirector.execution.adapter;

import java.util.Arrays;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.composition.ControlledCompositionExecutor;
import com.sobrenaturaldirector.composition.GraveStoneCompositionPolicy;
import com.sobrenaturaldirector.composition.CompositionJournalEntry;
import com.sobrenaturaldirector.composition.model.*;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.execution.*;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;

/** Production structure adapter reusing the existing allowlist/journal executor. */
public final class GraveStoneExecutionSlicePreparer implements ExecutionSlicePreparer {
    private final DirectorProviderRegistry registry;
    public GraveStoneExecutionSlicePreparer(DirectorProviderRegistry registry) { if (registry == null) throw new IllegalArgumentException("registry is required"); this.registry = registry; }
    public boolean supports(CapabilityDescriptor capability) { return capability != null && CapabilityVocabulary.STRUCTURE_SOURCE.equals(capability.getId()) && new ProviderId("gravestone").equals(capability.getProvider()); }
    public ControlledProviderSlice prepare(final CandidatePlan plan, CapabilityDescriptor capability, final ExecutionPreparationContext context) {
        final String id = context.getExecutionId() + ":structure";
        final DirectorOwnedCompositionPlan composition = fixture(id, context);
        final ControlledCompositionExecutor executor = new ControlledCompositionExecutor(com.sobrenaturaldirector.runtime.DirectorRuntimeMode.CONTROLLED_EXECUTION, new GraveStoneCompositionPolicy());
        return new ControlledProviderSlice() {
            public String getSliceId() { return id; }
            public CoordinatedSlicePhase getPhase() { return CoordinatedSlicePhase.STRUCTURE; }
            public ProviderId getProviderId() { return new ProviderId("gravestone"); }
            public CoordinatedSliceResult preflight() { if (registry.get(new ProviderId("gravestone")) == null || context.getWorld() == null || context.getSaved() == null || !new GraveStoneCompositionPolicy().isProviderAvailable()) return fail("structure provider unavailable"); return ok("structure preflight"); }
            public CoordinatedSliceResult execute() { CompositionExecutionResult result = executor.execute(context.getWorld(), context.getSaved(), composition, true); return result.getStatus() == CompositionExecutionResult.Status.EXECUTED || result.getStatus() == CompositionExecutionResult.Status.ALREADY_EXECUTED ? ok(result.getStatus().name()) : fail(result.getStatus().name()); }
            public CoordinatedSliceResult reconcile() { CompositionJournalEntry entry = context.getSaved().getComposition(id); return entry != null && "EXECUTED".equals(entry.getStatus()) ? ok("reconciled") : fail("structure not committed"); }
            public CoordinatedSliceResult compensate() { CompositionExecutionResult result = executor.rollback(context.getWorld(), context.getSaved(), id); return result.getStatus() == CompositionExecutionResult.Status.ROLLBACK_EXECUTED ? CoordinatedSliceResult.of(CoordinatedSliceResult.Status.COMPENSATED, result.getStatus().name()) : fail(result.getStatus().name()); }
        };
    }
    private static DirectorOwnedCompositionPlan fixture(String id, ExecutionPreparationContext context) {
        ExternalBlockReference block = new ExternalBlockReference("GraveStone", "GSBoneBlock", 0);
        return new DirectorOwnedCompositionPlan(id, "CONTROLLED_MULTI_PROVIDER", "GraveStone", "2.13.0", 0, context.getAnchorX(), context.getAnchorY(), context.getAnchorZ(), Arrays.asList(new CompositionBlockOperation(id + ":block0", 0, 0, 0, block), new CompositionBlockOperation(id + ":block1", 1, 0, 0, block), new CompositionBlockOperation(id + ":block2", 2, 0, 0, block)));
    }
    private static CoordinatedSliceResult ok(String detail) { return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.EXECUTED, detail); }
    private static CoordinatedSliceResult fail(String detail) { return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.FAILED, detail); }
}
