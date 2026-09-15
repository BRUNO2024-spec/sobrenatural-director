package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import com.sobrenaturaldirector.decision.model.Intent;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.situation.SituationContext;
import com.sobrenaturaldirector.situation.SituationMemory;

/** Bounded autonomous cognition. This class deliberately has no gameplay dependency. */
public final class ControlledAutonomousPlanningScheduler {
    public static final int CADENCE_TICKS = 200;
    public static final int MAX_SCOPES_PER_CYCLE = 1;
    public static final int MAX_THREADS_PER_SCOPE = 8;
    public static final int MAX_OPPORTUNITIES_PER_SCOPE = 16;
    public static final int MAX_PLAN_TRANSITIONS_PER_SCOPE_CYCLE = 2;
    public static final int MAX_REARBITRATIONS_PER_SCOPE_CYCLE = 1;
    public static final int MAX_NEW_PLANS_PER_SCOPE_CYCLE = 1;
    public static final int MAX_RECENT_CYCLE_HISTORY = 32;

    public enum Decision {
        DISABLED, SKIPPED_NOT_DUE, SKIPPED_NO_CHANGE, NO_ACTION,
        NO_ELIGIBLE_OPPORTUNITY, PACING_DEFERRED, EXISTING_PLAN_RETAINED,
        PLAN_CREATED, ERROR_SAFE_ABORT
    }

    private final NarrativeIntentArbiter arbiter = new NarrativeIntentArbiter();
    private final NarrativePlanSynthesizer synthesizer = new NarrativePlanSynthesizer();
    private final NarrativePlanLifecycleCoordinator lifecycle = new NarrativePlanLifecycleCoordinator();
    private final NarrativeThreadManager threads = new NarrativeThreadManager();
    private final AdaptivePacingDirector pacing = new AdaptivePacingDirector();
    private final boolean enabled;
    private final Map<String, ScopeState> scopes = new HashMap<String, ScopeState>();
    private long cycleId;
    private String lastSelectedThread = "";

    public ControlledAutonomousPlanningScheduler(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }
    public long getCycleId() { return cycleId; }
    public long getNextTick() { return scopes.isEmpty() ? Long.MIN_VALUE : scopes.values().iterator().next().nextTick; }
    public String getLastFingerprint() { return scopes.isEmpty() ? "" : scopes.values().iterator().next().fingerprint; }
    public String getLastSelectedThread() { return lastSelectedThread; }

    public PlanningCycleResult evaluate(CycleInput input) {
        if (input == null) return PlanningCycleResult.error(0, "", Decision.ERROR_SAFE_ABORT, "NULL_INPUT");
        if (!enabled) return PlanningCycleResult.of(++cycleId, input.scope, Decision.DISABLED, "DISABLED", null, null);
        ScopeState scope = scopes.get(input.scope);
        if (scope == null) { scope = new ScopeState(); DirectorWorldSavedData.SchedulerMetadata saved = input.data.getSchedulerMetadata(input.scope); if (saved != null) { scope.fingerprint=saved.getFingerprint(); scope.nextTick=saved.getLastTick()+CADENCE_TICKS; scope.backoffTicks=saved.getBackoffTicks(); } scopes.put(input.scope, scope); }
        if (input.tick < scope.nextTick && input.fingerprint.equals(scope.fingerprint)) return PlanningCycleResult.of(++cycleId, input.scope, Decision.SKIPPED_NOT_DUE, "CADENCE_BACKOFF", null, null);
        if (input.fingerprint.equals(scope.fingerprint)) { scope.backoffTicks=Math.min(1600, Math.max(CADENCE_TICKS, scope.backoffTicks*2)); scope.nextTick=input.tick+scope.backoffTicks; input.data.setSchedulerMetadata(input.scope,input.tick,input.fingerprint,scope.backoffTicks); return PlanningCycleResult.of(++cycleId, input.scope, Decision.SKIPPED_NO_CHANGE, "UNCHANGED_FINGERPRINT_BACKOFF", null, null); }
        scope.fingerprint=input.fingerprint; scope.backoffTicks=CADENCE_TICKS; scope.nextTick=input.tick+CADENCE_TICKS; input.data.setSchedulerMetadata(input.scope, input.tick, input.fingerprint, scope.backoffTicks);
        try {
            String selectedThread = input.threadId;
            if (!input.candidateThreads.isEmpty()) {
                ThreadAttentionDecision attention = pacing.assessThreads(input.arbitration.getDecision(), input.candidateThreads, input.relevantSubjects);
                selectedThread = attention.getSelectedThreadId();
                lastSelectedThread = selectedThread;
            }
            List<NarrativeOpportunity> opportunities = input.opportunities;
            PersistentNarrativePlan existing = input.currentPlan;
            if (existing != null && !existing.getState().isTerminal()) {
                lifecycle.revalidate(existing, input.registry, input.situation.getRegionProfile(), input.tick, input.data);
                existing = input.data.getNarrativePlan(existing.getId());
            }
            NarrativeIntentDecision decision = arbiter.arbitrate(input.arbitration, opportunities);
            if (decision.isNoAction()) {
                Decision result = decision.getStatus() == NarrativeIntentDecision.Status.NO_ELIGIBLE_OPPORTUNITY
                        ? Decision.NO_ELIGIBLE_OPPORTUNITY : Decision.NO_ACTION;
                return PlanningCycleResult.of(++cycleId, input.scope, result, decision.getStatus().name(), decision, null);
            }
            if (existing != null && !existing.getState().isTerminal()
                    && existing.getState() != NarrativePlanLifecycleState.STALE
                    && existing.getIntent().equals(decision.getIntent().name())) {
                PersistentNarrativePlan retained = lifecycle.applyPacing(existing, input.arbitration.getAdaptivePacing(), input.tick, input.data);
                return PlanningCycleResult.of(++cycleId, input.scope,
                        retained != null && retained.getState() == NarrativePlanLifecycleState.DEFERRED
                                ? Decision.PACING_DEFERRED : Decision.EXISTING_PLAN_RETAINED,
                        "EXISTING_PLAN_FIRST", decision, retained);
            }
            if (!allows(input.arbitration.getAdaptivePacing(), decision.getIntent()))
                return PlanningCycleResult.of(++cycleId, input.scope, Decision.PACING_DEFERRED, "PACING_SUPPRESSION", decision, null);
            NarrativePlanSynthesisRequest request = new NarrativePlanSynthesisRequest(decision.getIntent(), input.situation,
                    input.memory, input.registry, decision, existing != null, input.narrativeId, input.regionRelation,
                    input.synthesisKey, input.tags, 8, 3, 8, selectedThread);
            if (existing != null && existing.getState() == NarrativePlanLifecycleState.STALE) {
                PersistentNarrativePlan replacement = lifecycle.replan(existing, request, synthesizer, input.registry, input.data,
                        input.tick, input.tick + 12000);
                if (replacement == null) return PlanningCycleResult.of(++cycleId, input.scope, Decision.ERROR_SAFE_ABORT,
                        "REARBITRATION_REQUIRED", decision, null);
                if (replacement.getThreadId().length() > 0) threads.attach(input.data, replacement.getThreadId(), replacement, input.tick, "AUTONOMOUS_REPLAN");
                return PlanningCycleResult.of(++cycleId, input.scope, Decision.PLAN_CREATED,
                        existing.getIntent().equals(decision.getIntent().name()) ? "AUTONOMOUS_REPLAN" : "AUTONOMOUS_REARBITRATION", decision, replacement);
            }
            NarrativePlanSynthesisResult synthesized = synthesizer.synthesize(request);
            if (!synthesized.isSynthesized())
                return PlanningCycleResult.of(++cycleId, input.scope,
                        synthesized.getStatus() == NarrativePlanSynthesisResult.Status.NO_COMPATIBLE_BLUEPRINT
                                ? Decision.NO_ELIGIBLE_OPPORTUNITY : Decision.NO_ACTION,
                        synthesized.getStatus().name(), decision, null);
            String planId = "plan:auto:" + input.scope + ":" + input.tick;
            PersistentNarrativePlan plan = lifecycle.persist(synthesized, planId, input.tick, input.tick + 12000,
                    input.registry, input.data);
            String threadId = selectedThread.length() == 0 ? "thread:auto:" + input.scope : selectedThread;
            PersistentNarrativeThread thread = threads.create(input.data, threadId, input.narrativeId,
                    input.situation.getDimension(), input.situation.getRegionX(), input.situation.getRegionZ(),
                    input.tags, input.tick, "AUTONOMOUS_PLANNING");
            threads.attach(input.data, thread.getId(), plan, input.tick, "AUTONOMOUS_PLAN");
            return PlanningCycleResult.of(++cycleId, input.scope, Decision.PLAN_CREATED, "AUTONOMOUS_SYNTHESIS", decision,
                    input.data.getNarrativePlan(plan.getId()));
        } catch (RuntimeException failure) {
            return PlanningCycleResult.error(++cycleId, input.scope, Decision.ERROR_SAFE_ABORT, failure.getClass().getSimpleName());
        }
    }

    private static final class ScopeState { private long nextTick=Long.MIN_VALUE; private long backoffTicks=CADENCE_TICKS; private String fingerprint=""; }

    private static boolean allows(PacingAssessment pacing, Intent intent) {
        return pacing == null || pacing.allows(AdaptivePacingDirector.intensity(intent));
    }

    public static final class CycleInput {
        private final String scope, fingerprint, narrativeId, regionRelation, synthesisKey, threadId;
        private final long tick;
        private final List<NarrativeOpportunity> opportunities;
        private final NarrativeArbitrationContext arbitration;
        private final SituationContext situation;
        private final SituationMemory memory;
        private final DirectorProviderRegistry registry;
        private final Set<String> tags;
        private final PersistentNarrativePlan currentPlan;
        private final DirectorWorldSavedData data;
        private final List<PersistentNarrativeThread> candidateThreads;
        private final Set<String> relevantSubjects;
        public CycleInput(String scope, long tick, String fingerprint, List<NarrativeOpportunity> opportunities,
                NarrativeArbitrationContext arbitration, SituationContext situation, SituationMemory memory,
                DirectorProviderRegistry registry, String narrativeId, String regionRelation, String synthesisKey,
                Set<String> tags, String threadId, PersistentNarrativePlan currentPlan, DirectorWorldSavedData data) {
            this(scope,tick,fingerprint,opportunities,arbitration,situation,memory,registry,narrativeId,regionRelation,synthesisKey,tags,threadId,currentPlan,data,Collections.<PersistentNarrativeThread>emptyList(),Collections.<String>emptySet());
        }
        public CycleInput(String scope, long tick, String fingerprint, List<NarrativeOpportunity> opportunities,
                NarrativeArbitrationContext arbitration, SituationContext situation, SituationMemory memory,
                DirectorProviderRegistry registry, String narrativeId, String regionRelation, String synthesisKey,
                Set<String> tags, String threadId, PersistentNarrativePlan currentPlan, DirectorWorldSavedData data,
                List<PersistentNarrativeThread> candidateThreads, Set<String> relevantSubjects) {
            if (scope == null || fingerprint == null || arbitration == null || situation == null || registry == null || data == null)
                throw new IllegalArgumentException("scheduler input is incomplete");
            if (tick < 0) throw new IllegalArgumentException("tick must be non-negative");
            this.scope=scope; this.tick=tick; this.fingerprint=fingerprint; this.opportunities=Collections.unmodifiableList(
                    new ArrayList<NarrativeOpportunity>(opportunities == null ? Collections.<NarrativeOpportunity>emptyList() : opportunities));
            if (this.opportunities.size() > MAX_OPPORTUNITIES_PER_SCOPE) throw new IllegalArgumentException("opportunity limit exceeded");
            this.arbitration=arbitration; this.situation=situation; this.memory=memory == null ? new SituationMemory() : memory;
            this.registry=registry; this.narrativeId=narrativeId == null ? "" : narrativeId; this.regionRelation=regionRelation == null ? "" : regionRelation;
            this.synthesisKey=synthesisKey == null ? "" : synthesisKey; this.tags=tags == null ? Collections.<String>emptySet() : tags;
            this.threadId=threadId == null ? "" : threadId; this.currentPlan=currentPlan; this.data=data;
            this.candidateThreads=Collections.unmodifiableList(new ArrayList<PersistentNarrativeThread>(candidateThreads == null ? Collections.<PersistentNarrativeThread>emptyList() : candidateThreads));
            this.relevantSubjects=relevantSubjects == null ? Collections.<String>emptySet() : relevantSubjects;
            if (this.candidateThreads.size() > MAX_THREADS_PER_SCOPE) throw new IllegalArgumentException("thread limit exceeded");
        }
    }

    public static final class PlanningCycleResult {
        private final long cycleId; private final String scope, reason; private final Decision decision;
        private final NarrativeIntentDecision intent; private final PersistentNarrativePlan plan;
        private PlanningCycleResult(long id,String scope,Decision decision,String reason,NarrativeIntentDecision intent,PersistentNarrativePlan plan){this.cycleId=id;this.scope=scope;this.decision=decision;this.reason=reason;this.intent=intent;this.plan=plan;}
        static PlanningCycleResult of(long id,String scope,Decision decision,String reason,NarrativeIntentDecision intent,PersistentNarrativePlan plan){return new PlanningCycleResult(id,scope,decision,reason,intent,plan);}
        static PlanningCycleResult error(long id,String scope,Decision decision,String reason){return new PlanningCycleResult(id,scope,decision,reason,null,null);}
        public long getCycleId(){return cycleId;} public String getScope(){return scope;} public Decision getDecision(){return decision;} public String getReason(){return reason;} public NarrativeIntentDecision getIntent(){return intent;} public PersistentNarrativePlan getPlan(){return plan;}
    }
}
