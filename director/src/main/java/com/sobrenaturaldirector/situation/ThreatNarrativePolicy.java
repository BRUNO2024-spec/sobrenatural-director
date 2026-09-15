package com.sobrenaturaldirector.situation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sobrenaturaldirector.capability.DecisionTrace;

/** Deterministic, provider-neutral narrative gate for semantic threat roles. */
public final class ThreatNarrativePolicy {
    public static final long HARD_COOLDOWN_TICKS = 2400L;
    public static final long RECOVERY_TICKS = 12000L;
    public static final long CALM_TICKS = 24000L;
    public static final int RECENT_PENALTY = -20;
    public static final int SAME_REGION_PENALTY = -15;
    public static final int MULTIPLE_THREAT_PENALTY = -10;
    public static final int LONG_CALM_BONUS = 5;

    public ThreatNarrativeAssessment assess(SituationBlueprint blueprint, SituationContext context, SituationMemory memory, DecisionTrace trace) {
        if (blueprint == null || context == null || !hasThreat(blueprint)) return assessment(true, false, false, false, 0, 0, 0, Collections.<String>emptyList(), trace, blueprint);
        SituationMemory safeMemory = memory == null ? new SituationMemory() : memory;
        List<ConfirmedThreatOutcome> outcomes = safeMemory.confirmedThreats();
        List<String> reasons = new ArrayList<String>();
        int recent = 0, sameRegion = 0; long latestAge = Long.MAX_VALUE;
        for (ConfirmedThreatOutcome outcome : outcomes) if (outcome.isConfirmedThreat() && context.getTick() >= outcome.getTick()) {
            long age = context.getTick() - outcome.getTick();
            if (age <= RECOVERY_TICKS) { recent++; if (outcome.getRegionX() == context.getRegionX() && outcome.getRegionZ() == context.getRegionZ()) sameRegion++; }
            if (age < latestAge) latestAge = age;
        }
        boolean hard = latestAge <= HARD_COOLDOWN_TICKS;
        boolean recovery = latestAge <= RECOVERY_TICKS;
        int modifier = 0;
        if (hard) { reasons.add("THREAT_RECENTLY_CONFIRMED"); reasons.add("THREAT_HARD_COOLDOWN"); }
        else if (recovery) {
            modifier += RECENT_PENALTY; reasons.add("THREAT_RECOVERY_ACTIVE");
            if (blueprint.getIntensity() == SituationIntensity.MEDIUM) modifier -= 5;
            if (blueprint.getIntensity() == SituationIntensity.HIGH) modifier -= 10;
            if (blueprint.getIntensity() != SituationIntensity.LOW) reasons.add("THREAT_INTENSITY_RECOVERY_PENALTY");
        } else if (latestAge >= CALM_TICKS && latestAge != Long.MAX_VALUE) { modifier += LONG_CALM_BONUS; reasons.add("THREAT_AVAILABLE_AFTER_CALM"); }
        if (recent > 1) { modifier += MULTIPLE_THREAT_PENALTY; reasons.add("THREAT_REPETITION_PENALTY"); }
        if (sameRegion > 0) { modifier += SAME_REGION_PENALTY; reasons.add("THREAT_REGION_REPETITION"); }
        boolean required = blueprint.getRequiredRoles().contains(SemanticRole.THREAT);
        boolean eligible = !hard || !required;
        boolean optionalSuppressed = hard && !required;
        if (required && hard) reasons.add("THREAT_REQUIRED_INFEASIBLE_FOR_PACING");
        if (optionalSuppressed) reasons.add("THREAT_OPTIONAL_OMITTED_BY_PACING");
        return assessment(eligible, optionalSuppressed, hard, recovery, modifier, recent, sameRegion, reasons, trace, blueprint);
    }

    private ThreatNarrativeAssessment assessment(boolean eligible, boolean optionalSuppressed, boolean hard, boolean recovery, int modifier, int recent, int sameRegion, List<String> reasons, DecisionTrace trace, SituationBlueprint blueprint) {
        ThreatNarrativeAssessment result = new ThreatNarrativeAssessment(eligible, optionalSuppressed, hard, recovery, modifier, recent, sameRegion, reasons);
        if (trace != null && blueprint != null && hasThreat(blueprint)) trace.pacing(blueprint.getId(), result);
        return result;
    }
    private static boolean hasThreat(SituationBlueprint blueprint) { return blueprint.getRequiredRoles().contains(SemanticRole.THREAT) || blueprint.getOptionalRoles().contains(SemanticRole.THREAT); }
}
