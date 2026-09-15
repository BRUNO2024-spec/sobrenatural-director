package com.sobrenaturaldirector.situation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.sobrenaturaldirector.capability.CapabilityQuery;
import com.sobrenaturaldirector.capability.DecisionTrace;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;

/** Creates and ranks semantic blueprint candidates without provider branches. */
public final class SituationBlueprintComposer {
    private static final long BLUEPRINT_WINDOW = 24000L;
    private final RoleCapabilityResolver roles = new RoleCapabilityResolver();
    private final ThreatNarrativePolicy pacing = new ThreatNarrativePolicy();
    public List<BlueprintCandidate> compose(SituationGoal goal, SituationContext context, SituationMemory memory, DirectorProviderRegistry registry, DecisionTrace trace) {
        if (goal == null || context == null || registry == null) return Collections.emptyList();
        List<SituationBlueprint> blueprints = blueprintsFor(goal);
        List<BlueprintCandidate> result = new ArrayList<BlueprintCandidate>();
        for (SituationBlueprint blueprint : blueprints) result.add(score(blueprint, context, memory == null ? new SituationMemory() : memory, registry, trace));
        Collections.sort(result, new Comparator<BlueprintCandidate>() { public int compare(BlueprintCandidate a, BlueprintCandidate b) { int score = Integer.compare(b.getScore(), a.getScore()); return score != 0 ? score : a.getBlueprint().getId().compareTo(b.getBlueprint().getId()); } });
        return Collections.unmodifiableList(result);
    }
    public List<SituationBlueprint> blueprintsFor(SituationGoal goal) {
        if (goal == SituationGoal.INVESTIGATION) {
            List<SituationBlueprint> result = new ArrayList<SituationBlueprint>();
            result.add(new SituationBlueprint("investigation:poi-witness", goal, java.util.Arrays.asList(SemanticRole.POINT_OF_INTEREST, SemanticRole.WITNESS, SemanticRole.CLUE_SOURCE), Collections.<SemanticRole>emptyList(), SituationIntensity.LOW, 70, 1));
            result.add(new SituationBlueprint("investigation:ruin-victim", goal, java.util.Arrays.asList(SemanticRole.RUIN, SemanticRole.VICTIM, SemanticRole.DISCOVERY_TARGET), Collections.<SemanticRole>emptyList(), SituationIntensity.MEDIUM, 64, 2));
            return result;
        }
        if (goal == SituationGoal.DISCOVERY) {
            List<SituationBlueprint> result = new ArrayList<SituationBlueprint>();
            result.add(new SituationBlueprint("discovery:landmark", goal, Collections.singletonList(SemanticRole.POINT_OF_INTEREST), Arrays.asList(SemanticRole.WITNESS, SemanticRole.THREAT), SituationIntensity.LOW, 60, 0));
            result.add(new SituationBlueprint("discovery:threatened-landmark", goal, Arrays.asList(SemanticRole.POINT_OF_INTEREST, SemanticRole.THREAT), Collections.<SemanticRole>emptyList(), SituationIntensity.MEDIUM, 48, 1));
            return result;
        }
        if (goal == SituationGoal.THREAT_EVENT) {
            return Collections.singletonList(new SituationBlueprint("threat:perimeter", goal, Arrays.asList(SemanticRole.POINT_OF_INTEREST, SemanticRole.THREAT), Collections.<SemanticRole>emptyList(), SituationIntensity.MEDIUM, 55, 2));
        }
        return Collections.singletonList(new SituationBlueprint("ambient:quiet-point", goal, Collections.singletonList(SemanticRole.POINT_OF_INTEREST), Collections.<SemanticRole>emptyList(), SituationIntensity.LOW, 30, 0));
    }
    private BlueprintCandidate score(SituationBlueprint blueprint, SituationContext context, SituationMemory memory, DirectorProviderRegistry registry, DecisionTrace trace) {
        int score = blueprint.getBaseScore(); List<String> reasons = new ArrayList<String>(); reasons.add("base=" + score);
        ThreatNarrativeAssessment pacingAssessment = pacing.assess(blueprint, context, memory, trace); score += pacingAssessment.getScoreModifier(); reasons.addAll(pacingAssessment.getReasons());
        if (context.isSuitableRegion()) { score += 10; reasons.add("locationFit=10"); }
        if (context.isLowRecentActivity()) { score += 8; reasons.add("noveltyFit=8"); }
        int repeated = memory.recentBlueprintCount(blueprint.fingerprint(), context.getTick(), BLUEPRINT_WINDOW); if (repeated > 0) { score -= repeated * 20; reasons.add("similarityPenalty=" + (-repeated * 20)); }
        boolean feasible = true;
        if (!pacingAssessment.isEligible()) { feasible = false; reasons.add("infeasible for pacing"); }
        for (com.sobrenaturaldirector.content.model.SemanticCapability capability : roles.required(blueprint.getRequiredRoles())) if (registry.query(new CapabilityQuery(capability)).isEmpty()) { feasible = false; reasons.add("missing required capability " + capability); }
        for (SemanticRole role : blueprint.getOptionalRoles()) {
            List<com.sobrenaturaldirector.content.model.SemanticCapability> optional = roles.optional(Collections.singletonList(role));
            if (!optional.isEmpty() && pacingAssessment.isOptionalSuppressed() && role == SemanticRole.THREAT) {
                reasons.add("optional omitted by pacing policy " + role);
                if (trace != null) trace.optionalOmittedByPacing(role);
            } else if (!optional.isEmpty() && registry.query(new CapabilityQuery(optional.get(0))).isEmpty()) {
                reasons.add("optional omitted " + role + " missing capability " + optional.get(0));
                if (trace != null) trace.optionalSkipped(role, optional.get(0));
            }
        }
        if (feasible) { score += 15; reasons.add("capabilityFeasibility=15"); }
        if (trace != null) trace.blueprint(blueprint.getId(), blueprint.getRequiredRoles(), blueprint.getOptionalRoles(), blueprint.getIntensity().name(), score, reasons, feasible);
        return new BlueprintCandidate(blueprint, score, feasible, reasons);
    }
}
