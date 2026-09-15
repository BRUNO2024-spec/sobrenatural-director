package com.sobrenaturaldirector.decision.constraint;
import java.util.ArrayList; import java.util.List; import java.util.Map;
import com.sobrenaturaldirector.decision.model.*;
import com.sobrenaturaldirector.planning.environment.*;
public final class StandardConstraints {
    private StandardConstraints() {}
    public static List<ConstraintResult> evaluate(DecisionContext c, CandidateAction a) {
        List<ConstraintResult> r=new ArrayList<ConstraintResult>();
        boolean destructive=a.getSafety()!=ActionSafety.NON_DESTRUCTIVE;
        r.add(new ConstraintResult("safety",ConstraintCategory.SAFETY,!destructive || (c.isSafetyKnown() && !c.isObservingSite()),!destructive || !c.isSafetyKnown()?"SAFETY_CONTEXT_UNKNOWN":"SAFETY_OK"));
        String provider=a.getRequirements().get("provider"); boolean providerOk=provider==null || c.getProviders().get(provider)==ProviderStatus.AVAILABLE;
        r.add(new ConstraintResult("content",ConstraintCategory.CONTENT,providerOk && tagsAvailable(c,a),providerOk?"CONTENT_OK":"CONTENT_UNAVAILABLE"));
        String unique=a.getRequirements().get("uniqueClaim"); boolean uniqueOk=unique==null || !c.getUniqueClaims().contains(unique);
        r.add(new ConstraintResult("uniqueness",ConstraintCategory.UNIQUENESS,uniqueOk,uniqueOk?"UNIQUE_AVAILABLE":"UNIQUE_ALREADY_CLAIMED"));
        boolean budget=true; for(Map.Entry<String,Double> e:a.getEstimatedCosts().entrySet()) if(c.getBudgets().get(e.getKey())==null || c.getBudgets().get(e.getKey())<e.getValue()) budget=false;
        r.add(new ConstraintResult("budget",ConstraintCategory.BUDGET,budget,budget?"BUDGET_OK":"BUDGET_EXCEEDED"));
        Long until=c.getCooldownUntil().get(a.getIntent().name()); boolean cooldownOk=until==null || c.getTick()>=until;
        r.add(new ConstraintResult("cooldown",ConstraintCategory.COOLDOWN,cooldownOk,cooldownOk?"COOLDOWN_EXPIRED":"COOLDOWN_ACTIVE"));
        EnvironmentPlanningAssessment environment=new PlayerAreaSafetyPolicy().assess(c.getEnvironmentProfile(),a);
        r.add(new ConstraintResult("environment",ConstraintCategory.SAFETY,environment.isAllowed(),environment.isHardRejected()?environment.getReasons().get(0):environment.getReasons().get(0)));
        return r;
    }
    private static boolean tagsAvailable(DecisionContext c,CandidateAction a){String tags=a.getRequirements().get("tags"); if(tags==null)return true; for(String tag:tags.split(","))if(!c.getContentTags().contains(tag))return false; return true;}
}
