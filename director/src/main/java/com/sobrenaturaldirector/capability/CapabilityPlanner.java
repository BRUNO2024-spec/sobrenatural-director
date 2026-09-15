package com.sobrenaturaldirector.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.sobrenaturaldirector.content.model.SemanticCapability;

/** Pure deterministic planner. It selects capabilities; it never executes mutations. */
public final class CapabilityPlanner {
    private final com.sobrenaturaldirector.provider.DirectorProviderRegistry registry;
    public CapabilityPlanner(com.sobrenaturaldirector.provider.DirectorProviderRegistry registry) { if (registry == null) throw new IllegalArgumentException("registry is required"); this.registry = registry; }
    public List<CandidatePlan> plan(SituationIntent intent, DirectorContext context, List<CapabilityConstraint> constraints) {
        return plan(intent, context, constraints, null);
    }
    public List<CandidatePlan> plan(SituationIntent intent, DirectorContext context, List<CapabilityConstraint> constraints, DecisionTrace trace) {
        if (intent == null || context == null) return Collections.emptyList();
        ArrayList<List<CapabilityDescriptor>> options = new ArrayList<List<CapabilityDescriptor>>();
        for (SemanticCapability requirement : intent.getRequirements()) {
            List<CapabilityDescriptor> found = registry.query(new CapabilityQuery(requirement));
            if (trace != null) trace.candidates(requirement, found);
            if (found.isEmpty()) { if (trace != null) trace.rejected("missing capability " + requirement); return Collections.emptyList(); }
            options.add(found);
        }
        ArrayList<CandidatePlan> plans = new ArrayList<CandidatePlan>();
        expand(intent, context, constraints == null ? Collections.<CapabilityConstraint>emptyList() : constraints, options, 0, new LinkedHashMap<SemanticCapability, CapabilityDescriptor>(), plans, trace);
        Collections.sort(plans, new Comparator<CandidatePlan>() { public int compare(CandidatePlan a, CandidatePlan b) { int score = Integer.compare(b.getScore(), a.getScore()); return score != 0 ? score : a.signature().compareTo(b.signature()); } });
        return Collections.unmodifiableList(plans);
    }
    private void expand(SituationIntent intent, DirectorContext context, List<CapabilityConstraint> constraints, List<List<CapabilityDescriptor>> options, int index, Map<SemanticCapability, CapabilityDescriptor> selected, List<CandidatePlan> output, DecisionTrace trace) {
        if (index == options.size()) {
            ArrayList<CapabilityDescriptor> values = new ArrayList<CapabilityDescriptor>(selected.values());
            for (CapabilityConstraint constraint : constraints) if (constraint == null || !constraint.accepts(intent, context, values)) { if (trace != null) trace.rejected("constraint mismatch"); return; }
            if (trace != null) for (Map.Entry<SemanticCapability, CapabilityDescriptor> entry : selected.entrySet()) trace.selected(entry.getKey(), entry.getValue());
            ArrayList<String> satisfied = new ArrayList<String>(); for (int i = 0; i < constraints.size(); i++) satisfied.add("constraint:" + i);
            output.add(new CandidatePlan(intent, selected, satisfied)); return;
        }
        SemanticCapability requirement = intent.getRequirements().get(index);
        for (CapabilityDescriptor descriptor : options.get(index)) {
            selected.put(requirement, descriptor); expand(intent, context, constraints, options, index + 1, selected, output, trace); selected.remove(requirement);
        }
    }
    public boolean isCurrent(CandidatePlan plan) {
        if (plan == null || !plan.isComplete()) return false;
        for (CapabilityDescriptor selected : plan.getSelections().values()) {
            boolean present = false;
            for (CapabilityDescriptor current : registry.query(new CapabilityQuery(selected.getId()))) if (selected.getProvider().equals(current.getProvider()) && selected.getAvailability() == current.getAvailability()) { present = true; break; }
            if (!present) return false;
        }
        return true;
    }
}
