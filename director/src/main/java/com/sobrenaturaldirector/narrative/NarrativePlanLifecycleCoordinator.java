package com.sobrenaturaldirector.narrative;

import java.util.Collections;
import com.sobrenaturaldirector.environment.model.SemanticRegionProfile;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import java.util.List;

/** World-ledger lifecycle coordinator. It is deliberately PLAN_ONLY. */
public final class NarrativePlanLifecycleCoordinator {
    public static final int MAX_PLANS_PER_INVOCATION = 8;
    private final PersistentNarrativePlanValidator validator = new PersistentNarrativePlanValidator();

    public PersistentNarrativePlan persist(NarrativePlanSynthesisResult result, String planId, long tick, long expiryTick, DirectorProviderRegistry registry, DirectorWorldSavedData data) {
        if (registry == null || data == null) throw new IllegalArgumentException("registry and data are required");
        PersistentNarrativePlan plan=PersistentNarrativePlan.fromSynthesis(planId, result, tick, expiryTick, registry.getRevision()); data.recordNarrativePlan(plan); return plan;
    }

    public NarrativePlanValidationResult revalidate(PersistentNarrativePlan plan, DirectorProviderRegistry registry, SemanticRegionProfile profile, long tick, DirectorWorldSavedData data) {
        NarrativePlanValidationResult result=validator.validate(plan, registry, profile, tick, data);
        if (data != null && plan != null) { PersistentNarrativePlan current=data.getNarrativePlan(plan.getId()); if (current != null) { NarrativePlanLifecycleState next=result.isValid() ? NarrativePlanLifecycleState.ACTIVE : NarrativePlanLifecycleState.STALE; if (!result.getStatus().name().equals(current.getValidationStatus()) || !result.getReasons().equals(current.getValidationReasons()) || current.getState()!=next) data.updateNarrativePlan(current.validated(result.getStatus().name(), result.getReasons(), tick, next)); } }
        return result;
    }
    /** Applies timing preference after semantic validation; pacing never turns a valid plan stale. */
    public PersistentNarrativePlan applyPacing(PersistentNarrativePlan plan, PacingAssessment pacing, long tick, DirectorWorldSavedData data) {
        if(plan==null||data==null)return plan; PersistentNarrativePlan current=data.getNarrativePlan(plan.getId()); if(current==null||current.getState().isTerminal())return current;
        boolean defer=pacing!=null&&!pacing.allows(AdaptivePacingDirector.intensity(com.sobrenaturaldirector.decision.model.Intent.valueOf(current.getIntent())));
        NarrativePlanLifecycleState next=defer?NarrativePlanLifecycleState.DEFERRED:NarrativePlanLifecycleState.ACTIVE;
        if(current.getState()!=NarrativePlanLifecycleState.STALE&&current.getState()!=next)data.updateNarrativePlan(current.withState(next,tick));
        return data.getNarrativePlan(plan.getId());
    }

    /** Revalidates only non-terminal records and never scans the world. */
    public int evaluateBounded(DirectorWorldSavedData data, DirectorProviderRegistry registry, ProfileLookup profiles, long tick) {
        if (data == null || registry == null || profiles == null) return 0;
        int processed=0; List<PersistentNarrativePlan> plans=data.getNarrativePlans();
        for (PersistentNarrativePlan plan : plans) { if (processed >= MAX_PLANS_PER_INVOCATION) break; if (plan.getState().isTerminal()) continue; revalidate(plan, registry, profiles.get(plan.getDimension(), plan.getRegionX(), plan.getRegionZ()), tick, data); processed++; }
        return processed;
    }
    public interface ProfileLookup { SemanticRegionProfile get(int dimension, int regionX, int regionZ); }

    /** Replanning is a new persisted identity; the old record is never resurrected. */
    public PersistentNarrativePlan replan(PersistentNarrativePlan oldPlan, NarrativePlanSynthesisRequest request, NarrativePlanSynthesizer synthesizer, DirectorProviderRegistry registry, DirectorWorldSavedData data, long tick, long expiryTick) {
        if (oldPlan == null || request == null || synthesizer == null || registry == null || data == null) throw new IllegalArgumentException("replan inputs are required");
        PersistentNarrativePlan persisted = data.getNarrativePlan(oldPlan.getId());
        if (persisted != null) oldPlan = persisted;
        if (oldPlan.getState() == NarrativePlanLifecycleState.SUPERSEDED) return null;
        if (oldPlan.getState().isTerminal()) throw new IllegalStateException("terminal plan cannot be replanned");
        if (oldPlan.getReplanCount() >= PersistentNarrativePlan.MAX_REPLANS) throw new IllegalStateException("replan limit reached");
        data.updateNarrativePlan(oldPlan.withState(NarrativePlanLifecycleState.REPLANNING, tick));
        NarrativePlanSynthesisResult result=synthesizer.synthesize(request);
        if (!result.isSynthesized()) { String status=(result.getStatus()==NarrativePlanSynthesisResult.Status.NO_COMPATIBLE_BLUEPRINT || result.getStatus()==NarrativePlanSynthesisResult.Status.NO_VALID_COMPOSITION) ? "REARBITRATION_REQUIRED" : "REPLAN_REQUIRED"; data.updateNarrativePlan(oldPlan.validated(status, result.getReasons(), tick, NarrativePlanLifecycleState.STALE)); return null; }
        String id="plan:"+oldPlan.getId()+":"+(oldPlan.getReplanCount()+1)+":"+result.getPlanFingerprint();
        if (id.length()>128) id="plan:"+oldPlan.getId()+":"+(oldPlan.getReplanCount()+1);
        PersistentNarrativePlan replacement=PersistentNarrativePlan.fromSynthesis(id,result,tick,expiryTick,registry.getRevision()).withThreadId(oldPlan.getThreadId());
        replacement=new PersistentNarrativePlan(replacement.getId(),replacement.getNarrativeId(),replacement.getIntent(),replacement.getBlueprintId(),replacement.getBlueprintFingerprint(),replacement.getPlanFingerprint(),replacement.getCandidateSignature(),replacement.getRegionRelation(),replacement.getImpact(),replacement.getCapabilities(),replacement.isContinuation(),replacement.getState(),replacement.getCreatedTick(),replacement.getUpdatedTick(),replacement.getExpiryTick(),replacement.getLastValidatedTick(),replacement.getValidationStatus(),replacement.getValidationReasons(),oldPlan.getId(),oldPlan.getReplanCount()+1,replacement.getProviderRevision(),replacement.getDimension(),replacement.getRegionX(),replacement.getRegionZ(),replacement.getEnvironment(),replacement.getEnvironmentConfidence(),replacement.getRecentActivity()).withThreadId(oldPlan.getThreadId());
        data.recordNarrativePlan(replacement); data.updateNarrativePlan(oldPlan.supersededBy(replacement.getId(),tick)); return replacement;
    }
}
