package com.sobrenaturaldirector.execution;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.SituationIntent;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.content.model.SemanticCapability;
import com.sobrenaturaldirector.narrative.PersistentNarrativePlan;

/** Rehydrates the provider-neutral persisted selection at the execution boundary. */
public final class PersistentNarrativePlanExecutionBridge {
    private final PreparedExecutionPlanFactory factory;

    public PersistentNarrativePlanExecutionBridge(PreparedExecutionPlanFactory factory) {
        if (factory == null) throw new IllegalArgumentException("execution factory is required");
        this.factory = factory;
    }

    public CandidatePlan candidate(PersistentNarrativePlan plan) {
        if (plan == null) throw new IllegalArgumentException("persistent plan is required");
        List<SemanticCapability> requirements = new ArrayList<SemanticCapability>();
        Map<SemanticCapability, CapabilityDescriptor> selections =
                new LinkedHashMap<SemanticCapability, CapabilityDescriptor>();
        for (Map.Entry<String, PersistentNarrativePlan.CapabilityRecord> entry : plan.getCapabilities().entrySet()) {
            SemanticCapability capability = new SemanticCapability(entry.getKey());
            PersistentNarrativePlan.CapabilityRecord value = entry.getValue();
            requirements.add(capability);
            selections.put(capability, new CapabilityDescriptor(capability, new ProviderId(value.getProvider()),
                    value.getAvailability(), value.getPriority(), value.getCost()));
        }
        if (requirements.isEmpty()) throw new IllegalArgumentException("persistent plan has no capabilities");
        return new CandidatePlan(new SituationIntent("director:" + plan.getIntent().toLowerCase(Locale.ENGLISH), requirements), selections,
                plan.getValidationReasons());
    }

    public PreparedMultiProviderPlan prepare(PersistentNarrativePlan plan, ExecutionPreparationContext context) {
        CandidatePlan candidate = candidate(plan);
        return factory.prepare(candidate, context, plan.getPlanFingerprint());
    }
}
