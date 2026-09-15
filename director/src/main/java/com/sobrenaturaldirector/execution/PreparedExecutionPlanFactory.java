package com.sobrenaturaldirector.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;

/** Builds a complete executable plan from semantic capability selections. */
public final class PreparedExecutionPlanFactory {
    private final List<ExecutionSlicePreparer> preparers;
    public PreparedExecutionPlanFactory(List<ExecutionSlicePreparer> preparers) {
        if (preparers == null || preparers.isEmpty()) throw new IllegalArgumentException("preparers are required");
        this.preparers = Collections.unmodifiableList(new ArrayList<ExecutionSlicePreparer>(preparers));
    }
    public PreparedMultiProviderPlan prepare(CandidatePlan plan, ExecutionPreparationContext context, String fingerprint) {
        if (plan == null || context == null || !plan.isComplete()) throw new IllegalArgumentException("candidate plan is not executable");
        List<ControlledProviderSlice> slices = new ArrayList<ControlledProviderSlice>();
        for (com.sobrenaturaldirector.content.model.SemanticCapability requiredCapability : plan.getIntent().getRequirements()) {
            CapabilityDescriptor selected = plan.getSelections().get(requiredCapability);
            if (selected == null) throw new IllegalArgumentException("required execution capability is not selected: " + requiredCapability.getValue());
            ControlledProviderSlice slice = null;
            for (ExecutionSlicePreparer preparer : preparers) if (preparer.supports(selected)) { slice = preparer.prepare(plan, selected, context); break; }
            if (slice == null) throw new IllegalArgumentException("required execution slice could not be prepared: " + requiredCapability.getValue());
            slices.add(slice);
        }
        if (slices.isEmpty()) throw new IllegalArgumentException("no execution slices prepared");
        return new PreparedMultiProviderPlan(plan, fingerprint, slices);
    }
}
