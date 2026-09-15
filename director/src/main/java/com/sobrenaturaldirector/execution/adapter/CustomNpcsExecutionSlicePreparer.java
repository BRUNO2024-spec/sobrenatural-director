package com.sobrenaturaldirector.execution.adapter;

import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.execution.*;
import com.sobrenaturaldirector.mutation.runtime.MutationExecutionResult;
import com.sobrenaturaldirector.mutation.runtime.MutationOperation;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.ControlledNpcRequest;
import com.sobrenaturaldirector.provider.ProviderMutationResult;
import com.sobrenaturaldirector.provider.customnpcs.CustomNpcsProviderAdapter;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;

/** Production actor adapter; all ownership and cleanup remain in CustomNPCsRuntimeBridge. */
public final class CustomNpcsExecutionSlicePreparer implements ExecutionSlicePreparer {
    private final CustomNpcsProviderAdapter provider;
    private final DirectorProviderRegistry registry;
    public CustomNpcsExecutionSlicePreparer(CustomNpcsProviderAdapter provider, DirectorProviderRegistry registry) { if (provider == null || registry == null) throw new IllegalArgumentException("provider and registry are required"); this.provider = provider; this.registry = registry; }
    public boolean supports(CapabilityDescriptor capability) { return capability != null && CapabilityVocabulary.ACTOR_SOURCE.equals(capability.getId()) && provider.getProviderId().equals(capability.getProvider()); }
    public ControlledProviderSlice prepare(CandidatePlan plan, CapabilityDescriptor capability, final ExecutionPreparationContext context) {
        final String id = context.getExecutionId() + ":actor";
        final ControlledNpcRequest create = request(id, MutationOperation.SPAWN_ENTITY, context);
        final ControlledNpcRequest remove = request(id, MutationOperation.REMOVE_ENTITY, context);
        return new ControlledProviderSlice() {
            public String getSliceId() { return id; }
            public CoordinatedSlicePhase getPhase() { return CoordinatedSlicePhase.ACTOR; }
            public ProviderId getProviderId() { return provider.getProviderId(); }
            public CoordinatedSliceResult preflight() { return registry.get(provider.getProviderId()) == provider && provider.isAvailable() && context.getWorld() != null && context.getSaved() != null ? ok("actor preflight") : fail("actor provider unavailable"); }
            public CoordinatedSliceResult execute() { ProviderMutationResult result = provider.createControlledNpc(create, context.getSaved()); return result.getResult().getStatus() == MutationExecutionResult.Status.EXECUTED || result.getResult().getStatus() == MutationExecutionResult.Status.ALREADY_EXECUTED ? ok(result.getResult().getStatus().name()) : fail(result.getResult().getStatus().name()); }
            public CoordinatedSliceResult reconcile() { return context.getSaved().hasExecutedMutation(id) ? ok("reconciled") : fail("actor not committed"); }
            public CoordinatedSliceResult compensate() { ProviderMutationResult result = provider.removeControlledNpc(remove, context.getSaved()); return result.getResult().getStatus() == MutationExecutionResult.Status.ROLLBACK_EXECUTED ? CoordinatedSliceResult.of(CoordinatedSliceResult.Status.COMPENSATED, "actor removed") : fail(result.getResult().getStatus().name()); }
        };
    }
    private ControlledNpcRequest request(String id, MutationOperation operation, ExecutionPreparationContext context) {
        return new ControlledNpcRequest(id, id, "CONTROLLED_VALIDATION", provider.getProviderId(), operation, context.getWorld(), 0, context.getAnchorX() + 1, context.getAnchorY() + 1, context.getAnchorZ(), id, "Director MPE Actor", "DIRECTOR_NPC", CustomNpcsProviderAdapter.SUPPORTED_VERSION, DirectorRuntimeMode.CONTROLLED_EXECUTION, true);
    }
    private static CoordinatedSliceResult ok(String detail) { return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.EXECUTED, detail); }
    private static CoordinatedSliceResult fail(String detail) { return CoordinatedSliceResult.of(CoordinatedSliceResult.Status.FAILED, detail); }
}
