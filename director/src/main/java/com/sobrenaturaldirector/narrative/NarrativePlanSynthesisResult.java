package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.content.model.SemanticCapability;
import com.sobrenaturaldirector.planning.environment.EnvironmentActionImpact;
import com.sobrenaturaldirector.situation.SemanticRole;
import com.sobrenaturaldirector.situation.SituationBlueprint;

/** Explicit synthesis outcome; null is never used as failure signaling. */
public final class NarrativePlanSynthesisResult {
    public enum Status { SYNTHESIZED, NO_PLAN_REQUIRED, NO_COMPATIBLE_BLUEPRINT, MISSING_CAPABILITY, ENVIRONMENT_REJECTED, CONTINUITY_CONFLICT, NO_VALID_COMPOSITION, DEFERRED, INVALID_INPUT }
    private final Status status; private final NarrativePlanSynthesisRequest request; private final SituationBlueprint blueprint; private final CandidatePlan plan;
    private final List<SemanticRole> requiredRoles, optionalRoles; private final Map<SemanticCapability,CapabilityDescriptor> selections; private final List<String> reasons, attempts; private final int fallbackCount; private final String relation, fingerprint; private final EnvironmentActionImpact impact;
    public NarrativePlanSynthesisResult(Status status,NarrativePlanSynthesisRequest request,SituationBlueprint blueprint,CandidatePlan plan,List<SemanticRole> requiredRoles,List<SemanticRole> optionalRoles,Map<SemanticCapability,CapabilityDescriptor> selections,List<String> reasons,List<String> attempts,int fallbackCount,String relation,EnvironmentActionImpact impact,String fingerprint){
        if(status==null||reasons==null||attempts==null||fallbackCount<0)throw new IllegalArgumentException("invalid synthesis result");this.status=status;this.request=request;this.blueprint=blueprint;this.plan=plan;this.requiredRoles=immutable(requiredRoles);this.optionalRoles=immutable(optionalRoles);this.selections=Collections.unmodifiableMap(new LinkedHashMap<SemanticCapability,CapabilityDescriptor>(selections==null?Collections.<SemanticCapability,CapabilityDescriptor>emptyMap():selections));this.reasons=immutable(reasons);this.attempts=immutable(attempts);this.fallbackCount=fallbackCount;this.relation=relation==null?"":relation;this.impact=impact;this.fingerprint=fingerprint==null?"":fingerprint;
    }
    private static <T> List<T> immutable(List<T> v){return Collections.unmodifiableList(new ArrayList<T>(v==null?Collections.<T>emptyList():v));}
    public Status getStatus(){return status;} public NarrativePlanSynthesisRequest getRequest(){return request;} public SituationBlueprint getBlueprint(){return blueprint;} public CandidatePlan getCandidatePlan(){return plan;}
    public List<SemanticRole> getRequiredRoles(){return requiredRoles;} public List<SemanticRole> getOptionalRoles(){return optionalRoles;} public Map<SemanticCapability,CapabilityDescriptor> getSelections(){return selections;} public List<String> getReasons(){return reasons;} public List<String> getAttempts(){return attempts;} public int getFallbackCount(){return fallbackCount;} public String getRegionRelation(){return relation;} public EnvironmentActionImpact getImpact(){return impact;} public String getPlanFingerprint(){return fingerprint;} public boolean isSynthesized(){return status==Status.SYNTHESIZED;}
}
