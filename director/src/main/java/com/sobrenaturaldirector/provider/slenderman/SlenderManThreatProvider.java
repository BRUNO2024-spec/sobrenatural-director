package com.sobrenaturaldirector.provider.slenderman;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.DirectorContentProvider;
import com.sobrenaturaldirector.provider.ProviderCapabilityState;
import com.sobrenaturaldirector.threat.ThreatDefinition;
import com.sobrenaturaldirector.threat.ThreatExecutionRequest;
import com.sobrenaturaldirector.threat.ThreatExecutionResult;
import com.sobrenaturaldirector.threat.ThreatJournalEntry;
import com.sobrenaturaldirector.threat.ThreatLifecycleState;
import com.sobrenaturaldirector.threat.ThreatOwnership;
import com.sobrenaturaldirector.threat.ThreatProvider;
import com.sobrenaturaldirector.threat.PersistentNarrativeThreat;
import com.sobrenaturaldirector.threat.ThreatPhysicalBinding;
import com.sobrenaturaldirector.threat.ThreatPhysicalObservation;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;
import org.apache.logging.log4j.Level;

/** Optional provider exposing exactly one validated SlenderMan threat definition. */
public final class SlenderManThreatProvider implements ThreatProvider {
    public static final ProviderId PROVIDER_ID = new ProviderId("slenderman");
    public static final String MOD_ID = "dg_slender";
    public static final String SUPPORTED_VERSION = "3.3_1.7.10";
    private static final String RUNTIME_VERSION = "v3.3 1.7.10";
    public static final String DEFINITION_ID = "slenderman";
    private final boolean enabled, executionEnabled;
    private final boolean faultInjection;
    private final Map<String, ProviderCapabilityState> capabilities;
    private final ThreatDefinition definition = new ThreatDefinition(PROVIDER_ID, DEFINITION_ID, "Slender Man");
    private SlenderManRuntimeBridge bridge;
    private ProviderStatus status = ProviderStatus.UNKNOWN;
    private String detectedVersion;
    private String initializationFailure;
    private Boolean startupGate;
    public static boolean isSupportedVersion(String version) { return SUPPORTED_VERSION.equals(version) || RUNTIME_VERSION.equals(version); }

    public SlenderManThreatProvider(boolean enabled, boolean executionEnabled) {
        this(enabled, executionEnabled, false);
    }
    public SlenderManThreatProvider(boolean enabled, boolean executionEnabled, boolean faultInjection) {
        this.enabled = enabled; this.executionEnabled = executionEnabled;
        this.faultInjection = faultInjection;
        Map<String, ProviderCapabilityState> values = new LinkedHashMap<String, ProviderCapabilityState>();
        values.put(CapabilityVocabulary.THREAT_SOURCE.getValue(), ProviderCapabilityState.MUTATION_VALIDATED);
        capabilities = Collections.unmodifiableMap(values);
        initialize();
    }

    public void initialize() {
        if (!enabled) { status = ProviderStatus.DISABLED; return; }
        try {
            if (!Loader.isModLoaded(MOD_ID)) { status = ProviderStatus.MISSING; return; }
            detectedVersion = Loader.instance().getIndexedModList().get(MOD_ID).getVersion();
            if (!isSupportedVersion(detectedVersion)) { status = ProviderStatus.AVAILABLE_UNSUPPORTED_VERSION; return; }
            bridge = new SlenderManRuntimeBridge();
            startupGate = bridge.openStartupGate();
            if (startupGate == null) { status = ProviderStatus.FAILED_INITIALIZATION; return; }
            FMLCommonHandler.instance().bus().register(this);
            status = ProviderStatus.AVAILABLE_SUPPORTED;
        } catch (RuntimeException failure) {
            if (startupGate != null && bridge != null) try { bridge.closeStartupGate(startupGate.booleanValue()); } catch (RuntimeException ignored) { }
            startupGate = null; initializationFailure = failure.toString(); FMLLog.log("sobrenaturaldirector", Level.ERROR, "SlenderMan provider initialization failed: %s", failure.toString()); status = ProviderStatus.FAILED_INITIALIZATION;
        }
    }

    @Override public void onServerStarted() {
        if (startupGate != null) { bridge.closeStartupGate(startupGate.booleanValue()); startupGate = null; }
    }

    public ThreatExecutionResult execute(ThreatExecutionRequest request, DirectorWorldSavedData saved) {
        if (request == null) return rejected(request, ThreatExecutionResult.Status.REJECTED_POLICY);
        if (!executionEnabled) return rejected(request, ThreatExecutionResult.Status.REJECTED_DISABLED);
        if (saved == null) return rejected(request, ThreatExecutionResult.Status.REJECTED_POLICY);
        if (request.getMode() != DirectorRuntimeMode.CONTROLLED_EXECUTION || !request.isAuthorized()) return rejected(request, ThreatExecutionResult.Status.REJECTED_POLICY);
        if (!isServerThread()) return rejected(request, ThreatExecutionResult.Status.REJECTED_WRONG_THREAD);
        if (!allowed(request, saved)) return rejected(request, ThreatExecutionResult.Status.REJECTED_POLICY);
        ThreatJournalEntry existing = saved.getThreat(request.getRequestId());
        if (existing != null) {
            if (existing.getState() == ThreatLifecycleState.RESOLVED) return result(ThreatExecutionResult.Status.ALREADY_EXECUTED, request, existing.getEntityUuid());
            Entity entity = bridge.find(request.getWorld(), existing.getEntityUuid());
            if (existing.getState() == ThreatLifecycleState.ACTIVE && bridge.owns(entity, request, existing.getThreatInstanceId())) return result(ThreatExecutionResult.Status.RECONCILED_EXISTING, request, existing.getEntityUuid());
            return result(ThreatExecutionResult.Status.FAILED, request, null);
        }
        for (ThreatJournalEntry entry : saved.getThreatJournal()) if (entry.getState() == ThreatLifecycleState.ACTIVE) return rejected(request, ThreatExecutionResult.Status.REJECTED_ACTIVE_CAP);
        int[] site = bridge.findSafeSite(request.getWorld(), request.getX(), request.getY(), request.getZ());
        if (site == null) return rejected(request, ThreatExecutionResult.Status.REJECTED_NO_SAFE_SITE);
        String instance = UUID.randomUUID().toString();
        ThreatJournalEntry prepared = new ThreatJournalEntry(request.getRequestId(), instance, PROVIDER_ID, DEFINITION_ID, null, ThreatOwnership.ORIGIN_VALUE, ThreatLifecycleState.PREPARED, request.getDimension(), site[0], site[1], site[2], request.getWorld().getTotalWorldTime(), 0L);
        saved.recordThreat(prepared);
        try {
            Entity entity = bridge.create(request, site, faultInjection); bridge.mark(entity, request, instance);
            if (!request.getWorld().spawnEntityInWorld(entity)) throw new IllegalStateException("SlenderMan insertion rejected");
            ThreatJournalEntry active = prepared.withState(ThreatLifecycleState.ACTIVE, entity.getUniqueID(), request.getWorld().getTotalWorldTime()); saved.updateThreat(active);
            return result(ThreatExecutionResult.Status.CREATED, request, entity.getUniqueID());
        } catch (RuntimeException failure) { saved.updateThreat(prepared.withState(ThreatLifecycleState.STALE, null, request.getWorld().getTotalWorldTime())); return rejected(request, ThreatExecutionResult.Status.FAILED); }
    }

    public ThreatExecutionResult cleanup(ThreatExecutionRequest request, DirectorWorldSavedData saved) {
        if (request == null) return rejected(request, ThreatExecutionResult.Status.REJECTED_POLICY);
        if (!executionEnabled) return rejected(request, ThreatExecutionResult.Status.REJECTED_DISABLED);
        if (saved == null) return rejected(request, ThreatExecutionResult.Status.REJECTED_POLICY);
        if (request.getMode() != DirectorRuntimeMode.CONTROLLED_EXECUTION || !request.isAuthorized()) return rejected(request, ThreatExecutionResult.Status.REJECTED_POLICY);
        if (!isServerThread()) return rejected(request, ThreatExecutionResult.Status.REJECTED_WRONG_THREAD);
        if (!allowed(request, saved)) return rejected(request, ThreatExecutionResult.Status.REJECTED_POLICY);
        ThreatJournalEntry entry = saved.getThreat(request.getRequestId());
        if (entry == null || entry.getState() != ThreatLifecycleState.ACTIVE) return rejected(request, ThreatExecutionResult.Status.REJECTED_POLICY);
        Entity entity = bridge.find(request.getWorld(), entry.getEntityUuid());
        if (entity == null) return result(ThreatExecutionResult.Status.ENTITY_NOT_LOADED, request, null);
        if (!bridge.owns(entity, request, entry.getThreatInstanceId())) return result(ThreatExecutionResult.Status.OWNERSHIP_MISMATCH, request, null);
        entity.setDead(); saved.updateThreat(entry.withState(ThreatLifecycleState.RESOLVED, entry.getEntityUuid(), request.getWorld().getTotalWorldTime()));
        return result(ThreatExecutionResult.Status.CLEANUP_EXECUTED, request, entry.getEntityUuid());
    }

    @Override public ThreatPhysicalObservation inspectBinding(PersistentNarrativeThreat threat, ThreatPhysicalBinding binding, WorldServer world) {
        if (!isAvailable() || bridge == null) return ThreatPhysicalObservation.PROVIDER_UNAVAILABLE;
        if (world == null) return ThreatPhysicalObservation.WORLD_UNAVAILABLE;
        if (world.provider.dimensionId != binding.getDimension()) return ThreatPhysicalObservation.WORLD_UNAVAILABLE;
        Entity entity = bridge.find(world, binding.getEntityUuid());
        if (entity == null) {
            return world.getChunkProvider().chunkExists(binding.getX() >> 4, binding.getZ() >> 4)
                    ? ThreatPhysicalObservation.ENTITY_ABSENT : ThreatPhysicalObservation.CHUNK_UNAVAILABLE;
        }
        if (entity.isDead) return ThreatPhysicalObservation.ENTITY_DEAD;
        return bridge.owns(entity, threat) ? ThreatPhysicalObservation.ENTITY_PRESENT : ThreatPhysicalObservation.OWNERSHIP_MISMATCH;
    }

    private boolean allowed(ThreatExecutionRequest request, DirectorWorldSavedData saved) {
        return request != null && saved != null && executionEnabled && request.isAuthorized() && request.getMode() == DirectorRuntimeMode.CONTROLLED_EXECUTION
                && PROVIDER_ID.equals(request.getProviderId()) && DEFINITION_ID.equals(request.getDefinitionId()) && ThreatOwnership.ORIGIN_VALUE.equals(request.getOrigin())
                && SUPPORTED_VERSION.equals(request.getExpectedVersion()) && request.getWorld() != null && request.getDimension() == 0
                && request.getWorld().provider.dimensionId == 0 && status == ProviderStatus.AVAILABLE_SUPPORTED;
    }
    private boolean isServerThread() { MinecraftServer server = MinecraftServer.getServer(); return server != null && Thread.currentThread().getName().equals("Server thread"); }
    private ThreatExecutionResult rejected(ThreatExecutionRequest request, ThreatExecutionResult.Status status) { return result(status, request, null); }
    private ThreatExecutionResult result(ThreatExecutionResult.Status status, ThreatExecutionRequest request, UUID uuid) { return ThreatExecutionResult.of(status, request == null ? "" : request.getRequestId(), uuid); }
    public ProviderId getProviderId() { return PROVIDER_ID; }
    public String getModId() { return MOD_ID; }
    public boolean isAvailable() { return status == ProviderStatus.AVAILABLE_SUPPORTED; }
    public String getDetectedVersion() { return detectedVersion; }
    @Override public String getExecutionVersion() { return SUPPORTED_VERSION; }
    public String getInitializationFailure() { return initializationFailure; }
    public ProviderStatus getStatus() { return status; }
    public Map<String, ProviderCapabilityState> getCapabilities() { return capabilities; }
    public ThreatDefinition getThreatDefinition() { return definition; }
    public java.util.Set<CapabilityPolicyBinding> getPolicyBindings() { return Collections.singleton(new CapabilityPolicyBinding(CapabilityVocabulary.THREAT_SOURCE, "slenderman.controlled_threat_boundary")); }
}
