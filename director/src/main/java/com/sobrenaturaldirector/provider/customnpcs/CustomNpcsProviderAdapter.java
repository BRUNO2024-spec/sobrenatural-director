package com.sobrenaturaldirector.provider.customnpcs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.mutation.runtime.MutationExecutionResult;
import com.sobrenaturaldirector.mutation.runtime.MutationJournalEntry;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.ControlledNpcRequest;
import com.sobrenaturaldirector.provider.ControlledMutationProvider;
import com.sobrenaturaldirector.provider.DirectorContentProvider;
import com.sobrenaturaldirector.provider.ProviderCapabilityState;
import com.sobrenaturaldirector.provider.ProviderMutationResult;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;

/** Optional production provider facade. CustomNPCs classes are resolved only by the lazy bridge. */
public final class CustomNpcsProviderAdapter implements ControlledMutationProvider {
    public static final ProviderId PROVIDER_ID = new ProviderId("customnpcs");
    public static final String MOD_ID = "customnpcs";
    public static final String SUPPORTED_VERSION = "1.7.10d";
    public static final String IMPLEMENTATION_VERSION = "1L.1B";
    public static final String SOURCE = "CONTROLLED_VALIDATION";
    private static final String CAP_CREATE = "customnpcs:npc_create_controlled";
    private static final String CAP_REMOVE = "customnpcs:npc_remove_controlled";
    private static final String CAP_IDENTITY = "customnpcs:npc_identity";
    private static final String CAP_PERSISTENCE = "customnpcs:npc_persistence";
    private final boolean enabled;
    private final boolean executionEnabled;
    private final Map<String, ProviderCapabilityState> capabilities;
    private ProviderStatus status;
    private String detectedVersion;
    private CustomNpcsRuntimeBridge bridge;

    public CustomNpcsProviderAdapter(boolean enabled) {
        this(enabled, false);
    }

    public CustomNpcsProviderAdapter(boolean enabled, boolean executionEnabled) {
        this.enabled = enabled;
        this.executionEnabled = executionEnabled;
        Map<String, ProviderCapabilityState> values = new LinkedHashMap<String, ProviderCapabilityState>();
        values.put(CapabilityVocabulary.ACTOR_SOURCE.getValue(), ProviderCapabilityState.MUTATION_VALIDATED);
        capabilities = Collections.unmodifiableMap(values);
        initialize();
    }

    public void initialize() {
        detectedVersion = null;
        if (!enabled) { status = ProviderStatus.DISABLED; return; }
        try {
            if (!Loader.isModLoaded(MOD_ID)) { status = ProviderStatus.MISSING; return; }
            detectedVersion = Loader.instance().getIndexedModList().get(MOD_ID).getVersion();
            status = isSupportedVersion(detectedVersion) ? ProviderStatus.AVAILABLE_SUPPORTED : ProviderStatus.AVAILABLE_UNSUPPORTED_VERSION;
        } catch (RuntimeException failure) { status = ProviderStatus.FAILED_INITIALIZATION; }
    }
    private boolean supported() { return status == ProviderStatus.AVAILABLE_SUPPORTED; }
    public static boolean isSupportedVersion(String version) { return SUPPORTED_VERSION.equals(version); }
    private CustomNpcsRuntimeBridge bridge() { if (bridge == null) bridge = new CustomNpcsRuntimeBridge(); return bridge; }

    public ProviderMutationResult createControlledNpc(ControlledNpcRequest request, DirectorWorldSavedData saved) {
        if (request != null && saved != null && PROVIDER_ID.equals(request.getProviderId())
                && SOURCE.equals(request.getRequestSource()) && saved.hasExecutedMutation(request.getMutationId()))
            return new ProviderMutationResult(MutationExecutionResult.of(MutationExecutionResult.Status.ALREADY_EXECUTED, request.getMutationId()), null);
        if (!allowed(request, saved, com.sobrenaturaldirector.mutation.runtime.MutationOperation.SPAWN_ENTITY)) return rejected(request);
        MutationJournalEntry journal = new MutationJournalEntry(request.getMutationId(), request.getPlanId(), SOURCE,
                request.getOperation().name(), request.getDimension(), request.getX(), request.getY(), request.getZ(),
                "minecraft:none", 0, "customnpcs:CustomNpc", 0, "PREPARED", request.getWorld().getTotalWorldTime(), null, "NOT_REQUESTED");
        saved.recordMutation(journal);
        try {
            UUID uuid = bridge().create(request);
            if (uuid == null) throw new IllegalStateException("CustomNPC creation returned no identity");
            saved.updateMutation(journal.withStatus("EXECUTED", "NOT_REQUESTED", request.getWorld().getTotalWorldTime()));
            return new ProviderMutationResult(MutationExecutionResult.of(MutationExecutionResult.Status.EXECUTED, request.getMutationId()), uuid);
        } catch (RuntimeException failure) {
            FMLLog.log("sobrenaturaldirector", Level.ERROR, "CustomNPC controlled create failed: %s", failure.toString());
            saved.updateMutation(journal.withStatus("FAILED", "NOT_REQUESTED", request.getWorld().getTotalWorldTime()));
            return new ProviderMutationResult(MutationExecutionResult.of(MutationExecutionResult.Status.FAILED_WORLD_WRITE, request.getMutationId()), null);
        }
    }

    public ProviderMutationResult removeControlledNpc(ControlledNpcRequest request, DirectorWorldSavedData saved) {
        if (!allowed(request, saved, com.sobrenaturaldirector.mutation.runtime.MutationOperation.REMOVE_ENTITY)) return rejected(request);
        MutationJournalEntry entry = saved.getMutation(request.getMutationId());
        if (entry == null || !"EXECUTED".equals(entry.getStatus())) return rejected(request);
        try {
            UUID uuid = bridge().remove(request);
            if (uuid == null) return rejected(request);
            saved.updateMutation(entry.withStatus("ROLLED_BACK", "EXECUTED", request.getWorld().getTotalWorldTime()));
            return new ProviderMutationResult(MutationExecutionResult.of(MutationExecutionResult.Status.ROLLBACK_EXECUTED, request.getMutationId()), uuid);
        } catch (RuntimeException failure) { FMLLog.log("sobrenaturaldirector", Level.ERROR, "CustomNPC controlled remove failed: %s", failure.toString()); return rejected(request); }
    }

    private boolean allowed(ControlledNpcRequest request, DirectorWorldSavedData saved, com.sobrenaturaldirector.mutation.runtime.MutationOperation operation) {
        if (request == null || saved == null) return false;
        boolean journalOk = operation == com.sobrenaturaldirector.mutation.runtime.MutationOperation.SPAWN_ENTITY
                ? saved.getMutation(request.getMutationId()) == null
                : saved.getMutation(request.getMutationId()) != null
                    && "EXECUTED".equals(saved.getMutation(request.getMutationId()).getStatus());
        return executionEnabled && request.isAuthorized() && request.getMode() == DirectorRuntimeMode.CONTROLLED_EXECUTION
                && SOURCE.equals(request.getRequestSource()) && "DIRECTOR_NPC".equals(request.getOrigin())
                && PROVIDER_ID.equals(request.getProviderId()) && operation == request.getOperation() && supported()
                && SUPPORTED_VERSION.equals(request.getExpectedVersion()) && request.getDimension() == 0
                && request.getWorld() != null && request.getWorld().provider.dimensionId == 0
                && journalOk
                && request.getY() >= 0 && request.getY() < 256;
    }
    private ProviderMutationResult rejected(ControlledNpcRequest request) { return new ProviderMutationResult(MutationExecutionResult.of(MutationExecutionResult.Status.REJECTED_POLICY, request == null ? "" : request.getMutationId()), null); }
    public ProviderId getProviderId() { return PROVIDER_ID; }
    public String getModId() { return MOD_ID; }
    public boolean isAvailable() { return supported(); }
    public String getDetectedVersion() { return detectedVersion; }
    public ProviderStatus getStatus() { return status; }
    public Map<String, ProviderCapabilityState> getCapabilities() { return capabilities; }
    public java.util.Set<CapabilityPolicyBinding> getPolicyBindings() {
        java.util.Set<CapabilityPolicyBinding> values = new java.util.HashSet<CapabilityPolicyBinding>();
        values.add(new CapabilityPolicyBinding(CapabilityVocabulary.ACTOR_SOURCE, "customnpcs.controlled_actor_boundary"));
        return Collections.unmodifiableSet(values);
    }
}
