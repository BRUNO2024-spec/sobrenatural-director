package com.sobrenaturaldirector.situation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.sobrenaturaldirector.capability.CapabilityQuery;
import com.sobrenaturaldirector.capability.ProviderAvailability;
import com.sobrenaturaldirector.content.model.SemanticCapability;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;

/** Deterministic symbolic goal evaluator. */
public final class SituationEvaluator {
    private static final long REPETITION_WINDOW = 24000L;
    private final GoalRequirementResolver requirements = new GoalRequirementResolver();
    public List<SituationGoalCandidate> evaluate(SituationContext context, SituationMemory memory, DirectorProviderRegistry registry, com.sobrenaturaldirector.capability.DecisionTrace trace) {
        if (context == null || registry == null) return Collections.emptyList();
        SituationMemory safeMemory = memory == null ? new SituationMemory() : memory;
        List<SituationGoalCandidate> values = new ArrayList<SituationGoalCandidate>();
        values.add(candidate(SituationGoal.INVESTIGATION, context, safeMemory, registry, trace));
        values.add(candidate(SituationGoal.DISCOVERY, context, safeMemory, registry, trace));
        values.add(candidate(SituationGoal.AMBIENT_EVENT, context, safeMemory, registry, trace));
        Collections.sort(values, new Comparator<SituationGoalCandidate>() { public int compare(SituationGoalCandidate a, SituationGoalCandidate b) { int score = Integer.compare(b.getScore(), a.getScore()); return score != 0 ? score : a.getGoal().name().compareTo(b.getGoal().name()); } });
        return Collections.unmodifiableList(values);
    }
    private SituationGoalCandidate candidate(SituationGoal goal, SituationContext c, SituationMemory memory, DirectorProviderRegistry registry, com.sobrenaturaldirector.capability.DecisionTrace trace) {
        int score = goal == SituationGoal.INVESTIGATION ? 50 : goal == SituationGoal.DISCOVERY ? 45 : 30;
        List<String> reasons = new ArrayList<String>(); reasons.add("base=" + score);
        if (c.isNight() && goal == SituationGoal.INVESTIGATION) { score += 10; reasons.add("nightBonus=10"); }
        if (c.isNight() && goal == SituationGoal.DISCOVERY) { score -= 5; reasons.add("nightPenalty=-5"); }
        if (c.isSuitableRegion()) { score += goal == SituationGoal.AMBIENT_EVENT ? 5 : 10; reasons.add("suitabilityBonus=" + (goal == SituationGoal.AMBIENT_EVENT ? 5 : 10)); }
        if (c.isPlayerPresent() && goal != SituationGoal.AMBIENT_EVENT) { score += 10; reasons.add("playerBonus=10"); }
        if (c.isLowRecentActivity() && goal == SituationGoal.INVESTIGATION) { score += 8; reasons.add("noveltyBonus=8"); }
        int recent = memory.recentGoalCount(goal, c.getTick(), REPETITION_WINDOW); int local = memory.recentRegionCount(goal, c.getRegionX(), c.getRegionZ(), c.getTick(), REPETITION_WINDOW);
        if (recent > 0) { score -= recent * 15; reasons.add("recentPenalty=" + (-recent * 15)); }
        if (local > 0) { score -= local * 10; reasons.add("sameRegionPenalty=" + (-local * 10)); }
        boolean satisfiable = true;
        for (SemanticCapability capability : requirements.resolve(goal)) if (registry.query(new CapabilityQuery(capability, ProviderAvailability.SAFE_CAPABILITY)).isEmpty()) { satisfiable = false; reasons.add("missing=" + capability); }
        if (!requirements.resolve(goal).isEmpty() && satisfiable) { score += 20; reasons.add("capabilityAvailability=20"); }
        if (trace != null) trace.goal(goal.name(), score, reasons, satisfiable);
        return new SituationGoalCandidate(goal, score, satisfiable, reasons);
    }
}
