package com.sobrenaturaldirector.planning.environment;

import java.util.ArrayList;
import java.util.List;
import com.sobrenaturaldirector.decision.model.CandidateAction;
import com.sobrenaturaldirector.environment.model.EnvironmentClassification;
import com.sobrenaturaldirector.environment.model.SemanticRegionProfile;

/** Central planning-only environment policy. It never scans or mutates Minecraft. */
public final class PlayerAreaSafetyPolicy {
    public EnvironmentPlanningAssessment assess(SemanticRegionProfile profile,CandidateAction action){
        if(action==null)throw new IllegalArgumentException("action is required");
        EnvironmentActionImpact impact=EnvironmentActionImpact.from(action);List<String> reasons=new ArrayList<String>();
        if(profile==null){boolean hard=impact==EnvironmentActionImpact.DESTRUCTIVE||impact==EnvironmentActionImpact.HIGH_IMPACT;if(hard)reasons.add("ENVIRONMENT_UNKNOWN_HIGH_IMPACT");else reasons.add("ENVIRONMENT_UNKNOWN_NEUTRAL");return new EnvironmentPlanningAssessment(impact,EnvironmentClassification.UNKNOWN,0,hard?-40:0,!hard,hard,false,reasons);}
        EnvironmentClassification type=profile.getClassification();int confidence=profile.getConfidence();boolean established=type==EnvironmentClassification.ESTABLISHED_PLAYER_AREA&&confidence>=70;String relation=action.getMetadata().get("environmentRelation");boolean perimeter="PERIMETER".equals(relation)||"NEARBY_WILDERNESS".equals(relation);boolean hard=false;int modifier=0;
        if(established&&perimeter&&impact!=EnvironmentActionImpact.NON_INTRUSIVE){modifier=10;reasons.add("PERIMETER_PREFERRED");}
        else if(established){if(impact==EnvironmentActionImpact.HIGH_IMPACT||impact==EnvironmentActionImpact.DESTRUCTIVE){hard=true;reasons.add("ESTABLISHED_PLAYER_AREA_DESTRUCTIVE_ACTION");modifier=-60;}else if(impact==EnvironmentActionImpact.INTRUSIVE){modifier=-30;reasons.add("ESTABLISHED_AREA_INTRUSIVE_PENALTY");}else reasons.add("ESTABLISHED_AREA_LOW_IMPACT_ALLOWED");}
        else if(type==EnvironmentClassification.TEMPORARY_CAMP){modifier=impact==EnvironmentActionImpact.NON_INTRUSIVE?0:(impact==EnvironmentActionImpact.HIGH_IMPACT?-30:-12);reasons.add("TEMPORARY_CAMP_CONTEXT");}
        else if(type==EnvironmentClassification.PLAYER_MODIFIED_AREA){modifier=impact==EnvironmentActionImpact.NON_INTRUSIVE?0:(impact==EnvironmentActionImpact.HIGH_IMPACT?-25:-8);reasons.add("PLAYER_MODIFIED_AREA_CONTEXT");}
        else if(type==EnvironmentClassification.WILDERNESS){modifier=impact==EnvironmentActionImpact.NON_INTRUSIVE?5:0;reasons.add("WILDERNESS_SPATIAL_FREEDOM");}
        else if(type==EnvironmentClassification.VANILLA_VILLAGE){modifier=impact==EnvironmentActionImpact.HIGH_IMPACT?-15:0;reasons.add("VILLAGE_CONSERVATIVE_CONTEXT");}
        else {modifier=impact==EnvironmentActionImpact.HIGH_IMPACT?-20:0;reasons.add("ENVIRONMENT_UNCERTAIN_CONSERVATIVE");}
        return new EnvironmentPlanningAssessment(impact,type,confidence,Math.max(-60,Math.min(10,modifier)),!hard,hard,perimeter,reasons);
    }
}
