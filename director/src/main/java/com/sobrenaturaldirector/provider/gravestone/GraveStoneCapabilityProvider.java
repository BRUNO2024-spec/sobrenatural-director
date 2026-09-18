package com.sobrenaturaldirector.provider.gravestone;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.provider.DirectorContentProvider;
import com.sobrenaturaldirector.provider.ProviderCapabilityState;
import cpw.mods.fml.common.Loader;
import com.sobrenaturaldirector.control.*;
import com.sobrenaturaldirector.composition.ControlledCompositionExecutor;
import com.sobrenaturaldirector.composition.GraveStoneCompositionPolicy;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;

/** GraveStone metadata adapter; it exposes only capabilities proven by 1L.2A. */
public final class GraveStoneCapabilityProvider implements DirectorContentProvider {
    public static final ProviderId PROVIDER_ID = new ProviderId("gravestone");
    public static final String MOD_ID = "GraveStone";
    public static final String SUPPORTED_VERSION = "2.13.0";
    private final boolean enabled;
    private final boolean executionEnabled;
    private final Map<String, ProviderCapabilityState> capabilities;
    private ProviderStatus status;
    private String version;
    public GraveStoneCapabilityProvider(boolean enabled) {
        this(enabled, false);
    }
    public GraveStoneCapabilityProvider(boolean enabled, boolean executionEnabled) {
        this.enabled = enabled;
        this.executionEnabled = executionEnabled;
        Map<String, ProviderCapabilityState> values = new LinkedHashMap<String, ProviderCapabilityState>();
        values.put(CapabilityVocabulary.BLOCK_MUTATION.getValue(), ProviderCapabilityState.MUTATION_VALIDATED);
        values.put(CapabilityVocabulary.ROLLBACK_SAFE.getValue(), ProviderCapabilityState.MUTATION_VALIDATED);
        values.put(CapabilityVocabulary.STRUCTURE_SOURCE.getValue(), ProviderCapabilityState.MUTATION_VALIDATED);
        capabilities = Collections.unmodifiableMap(values);
        initialize();
    }
    public void initialize() {
        version = null;
        if (!enabled) { status = ProviderStatus.DISABLED; return; }
        try {
            if (!Loader.isModLoaded(MOD_ID)) { status = ProviderStatus.MISSING; return; }
            version = Loader.instance().getIndexedModList().get(MOD_ID).getVersion();
            status = SUPPORTED_VERSION.equals(version) ? ProviderStatus.AVAILABLE_SUPPORTED : ProviderStatus.AVAILABLE_UNSUPPORTED_VERSION;
        } catch (RuntimeException failure) { status = ProviderStatus.FAILED_INITIALIZATION; }
    }
    public ProviderId getProviderId() { return PROVIDER_ID; }
    public String getModId() { return MOD_ID; }
    public boolean isAvailable() { return status == ProviderStatus.AVAILABLE_SUPPORTED; }
    public String getDetectedVersion() { return version; }
    public ProviderStatus getStatus() { return status; }
    public Map<String, ProviderCapabilityState> getCapabilities() { return capabilities; }
    public java.util.Set<CapabilityPolicyBinding> getPolicyBindings() { java.util.Set<CapabilityPolicyBinding> values = new java.util.HashSet<CapabilityPolicyBinding>(); values.add(new CapabilityPolicyBinding(CapabilityVocabulary.BLOCK_MUTATION, "gravestone.allowlisted_blocks")); values.add(new CapabilityPolicyBinding(CapabilityVocabulary.ROLLBACK_SAFE, "director.stale_safe_rollback")); values.add(new CapabilityPolicyBinding(CapabilityVocabulary.STRUCTURE_SOURCE, "gravestone.allowlisted_structure")); return Collections.unmodifiableSet(values); }
    @Override public ControlResult executeControl(ControlRequest raw, ControlExecutionContext context, long tick) {
        if (!(raw instanceof StructureControlRequest) || context == null) return new ControlResult(ControlResultStatus.INVALID_REQUEST, "structure control requires server context");
        StructureControlRequest request = (StructureControlRequest) raw;
        if (!PROVIDER_ID.equals(request.getProvider()) || request.getPlan().getDimension() != context.getWorld().provider.dimensionId)
            return new ControlResult(ControlResultStatus.SAFETY_REJECTED, "structure ownership or dimension rejected");
        if (!executionEnabled || request.getAuthority() != ControlAuthority.FULL_DIRECTOR_CONTROL)
            return new ControlResult(ControlResultStatus.SAFETY_REJECTED, "controlled structure execution disabled");
        ControlledCompositionExecutor executor = new ControlledCompositionExecutor(DirectorRuntimeMode.CONTROLLED_EXECUTION, new GraveStoneCompositionPolicy());
        if (request.getOperation() == StructureControlRequest.Operation.APPLY) {
            com.sobrenaturaldirector.composition.model.CompositionExecutionResult result = executor.execute(context.getWorld(), context.getSaved(), request.getPlan(), true);
            return result.getStatus() == com.sobrenaturaldirector.composition.model.CompositionExecutionResult.Status.EXECUTED
                    ? new ControlResult(ControlResultStatus.APPLIED, result.getStatus().name())
                    : new ControlResult(ControlResultStatus.FAILED, result.getStatus().name());
        }
        com.sobrenaturaldirector.composition.model.CompositionExecutionResult result = executor.rollback(context.getWorld(), context.getSaved(), request.getPlan().getCompositionId());
        if (result.getStatus() == com.sobrenaturaldirector.composition.model.CompositionExecutionResult.Status.ROLLBACK_EXECUTED) return new ControlResult(ControlResultStatus.APPLIED, result.getStatus().name());
        if (result.getStatus() == com.sobrenaturaldirector.composition.model.CompositionExecutionResult.Status.ROLLBACK_REJECTED_STALE) return new ControlResult(ControlResultStatus.STALE_TARGET, result.getStatus().name());
        return new ControlResult(ControlResultStatus.FAILED, result.getStatus().name());
    }
}
