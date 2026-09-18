package com.sobrenaturaldirector.situation;

import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.capability.CapabilityPlanner;
import com.sobrenaturaldirector.capability.DecisionTrace;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.action.SemanticActionCatalog;
import com.sobrenaturaldirector.control.DimensionRef;

/** Advisory-only pipeline from immutable context to a capability plan. */
public final class SituationIntelligencePipeline {
    private final SituationEvaluator evaluator = new SituationEvaluator();
    private final SituationBlueprintComposer blueprints = new SituationBlueprintComposer();
    private final RoleCapabilityResolver roles = new RoleCapabilityResolver();
    public SituationDecision evaluate(SituationContext context, SituationMemory memory, DirectorProviderRegistry registry) {
        DecisionTrace trace = new DecisionTrace();
        return evaluate(context, memory, registry, trace);
    }
    public SituationDecision evaluate(SituationContext context, SituationMemory memory, DirectorProviderRegistry registry, DecisionTrace trace) {
        if (context == null || registry == null) return new SituationDecision(context, Collections.<SituationGoalCandidate>emptyList(), null, Collections.emptyList(), null);
        if (trace != null) trace.context("dimension=" + context.getDimension() + " tick=" + context.getTick() + " region=" + context.getRegionX() + ":" + context.getRegionZ() + " night=" + context.isNight() + " player=" + context.isPlayerPresent());
        List<SituationGoalCandidate> candidates = evaluator.evaluate(context, memory, registry, trace);
        SituationGoalCandidate selected = null;
        for (SituationGoalCandidate candidate : candidates) if (candidate.isSatisfiable()) { selected = candidate; break; }
        if (selected == null) { if (trace != null) trace.rejected("no satisfiable situation goal"); return new SituationDecision(context, candidates, null, Collections.emptyList(), null); }
        List<BlueprintCandidate> blueprintCandidates = blueprints.compose(selected.getGoal(), context, memory, registry, trace);
        BlueprintCandidate selectedBlueprint = null;
        for (BlueprintCandidate candidate : blueprintCandidates) if (candidate.isFeasible()) { selectedBlueprint = candidate; break; }
        if (selectedBlueprint == null) { if (trace != null) trace.rejected("no feasible blueprint"); return new SituationDecision(context, candidates, selected, Collections.emptyList(), null, null, blueprintCandidates); }
        List<com.sobrenaturaldirector.content.model.SemanticCapability> derived = roles.required(selectedBlueprint.getBlueprint().getRequiredRoles());
        ThreatNarrativeAssessment pacingAssessment = new ThreatNarrativePolicy().assess(selectedBlueprint.getBlueprint(), context, memory, null);
        for (SemanticRole role : selectedBlueprint.getBlueprint().getOptionalRoles()) {
            List<com.sobrenaturaldirector.content.model.SemanticCapability> optional = roles.optional(Collections.singletonList(role));
            if (!optional.isEmpty() && !(role == SemanticRole.THREAT && pacingAssessment.isOptionalSuppressed()) && !registry.query(new com.sobrenaturaldirector.capability.CapabilityQuery(optional.get(0))).isEmpty()) for (com.sobrenaturaldirector.content.model.SemanticCapability capability : optional) if (!derived.contains(capability)) derived = append(derived, capability);
        }
        if (trace != null) trace.requirements(selected.getGoal().name(), derived);
        List<CandidatePlan> plans = new CapabilityPlanner(registry).plan(new com.sobrenaturaldirector.capability.SituationIntent("situation:" + selected.getGoal().name(), derived), context.toPlannerContext(), null, trace);
        CandidatePlan plan = plans.isEmpty() ? null : plans.get(0);
        return new SituationDecision(context, candidates, selected, derived, plan, selectedBlueprint, blueprintCandidates);
    }
    /** Explicit opt-in composition entrypoint; evaluate() above remains plan-only. */
    public SituationInstance composeExecutable(SituationContext context, SituationMemory memory,
            DirectorProviderRegistry registry, SemanticActionCatalog catalog, String threadId) {
        SituationDecision decision = evaluate(context, memory, registry, new DecisionTrace());
        if (context == null) throw new IllegalArgumentException("context required");
        return new SituationComposer().compose(decision,
                new DimensionRef(context.getDimension(), "context", context.getEnvironment(), true), catalog, threadId);
    }
    private static List<com.sobrenaturaldirector.content.model.SemanticCapability> append(List<com.sobrenaturaldirector.content.model.SemanticCapability> source, com.sobrenaturaldirector.content.model.SemanticCapability value) { java.util.ArrayList<com.sobrenaturaldirector.content.model.SemanticCapability> result = new java.util.ArrayList<com.sobrenaturaldirector.content.model.SemanticCapability>(source); result.add(value); return result; }
}
