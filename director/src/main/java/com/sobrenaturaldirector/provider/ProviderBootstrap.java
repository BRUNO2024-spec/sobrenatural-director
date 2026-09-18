package com.sobrenaturaldirector.provider;

import com.sobrenaturaldirector.composition.ExternalBlockPolicy;
import com.sobrenaturaldirector.composition.GraveStoneCompositionPolicy;
import com.sobrenaturaldirector.config.FoundationConfig;
import com.sobrenaturaldirector.provider.customnpcs.CustomNpcsProviderAdapter;
import com.sobrenaturaldirector.provider.gravestone.GraveStoneCapabilityProvider;
import com.sobrenaturaldirector.provider.slenderman.SlenderManThreatProvider;
import com.sobrenaturaldirector.execution.PreparedExecutionPlanFactory;
import com.sobrenaturaldirector.execution.ExecutionSlicePreparer;
import com.sobrenaturaldirector.execution.adapter.GraveStoneExecutionSlicePreparer;
import com.sobrenaturaldirector.execution.adapter.CustomNpcsExecutionSlicePreparer;
import com.sobrenaturaldirector.execution.adapter.SlenderManExecutionSlicePreparer;

/** Provider-layer assembly point; the Director core depends only on this boundary. */
public final class ProviderBootstrap {
    private ProviderBootstrap() { }
    public static DirectorContentProvider registerAll(DirectorProviderRegistry registry, FoundationConfig configuration) {
        CustomNpcsProviderAdapter customNpcs = new CustomNpcsProviderAdapter(configuration.isCustomNpcsProviderEnabled(), configuration.isExecutionEnabled());
        registry.registerIfAvailable(customNpcs);
        registry.registerIfAvailable(new GraveStoneCapabilityProvider(true, configuration.isExecutionEnabled()));
        SlenderManThreatProvider slenderMan = new SlenderManThreatProvider(true, configuration.isExecutionEnabled());
        registry.registerIfAvailable(slenderMan);
        return customNpcs;
    }
    public static ExternalBlockPolicy blockPolicy() { return new GraveStoneCompositionPolicy(); }
    public static void onServerStarted(DirectorProviderRegistry registry) { if (registry != null) for (DirectorContentProvider provider : registry.getProviders().values()) provider.onServerStarted(); }
    /** Production assembly of semantic preparation adapters; execution remains in the coordinator. */
    public static PreparedExecutionPlanFactory executionFactory(DirectorProviderRegistry registry) {
        if (registry == null) throw new IllegalArgumentException("registry is required");
        DirectorContentProvider actor = registry.get(CustomNpcsProviderAdapter.PROVIDER_ID);
        if (!(actor instanceof CustomNpcsProviderAdapter)) throw new IllegalArgumentException("CustomNPCs actor adapter unavailable");
        java.util.List<ExecutionSlicePreparer> preparers = new java.util.ArrayList<ExecutionSlicePreparer>();
        preparers.add(new GraveStoneExecutionSlicePreparer(registry));
        preparers.add(new CustomNpcsExecutionSlicePreparer((CustomNpcsProviderAdapter) actor, registry));
        preparers.add(new SlenderManExecutionSlicePreparer(registry));
        return new PreparedExecutionPlanFactory(preparers);
    }
}
