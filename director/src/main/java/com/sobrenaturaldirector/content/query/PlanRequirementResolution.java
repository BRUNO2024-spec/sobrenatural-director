package com.sobrenaturaldirector.content.query;

import java.util.*;
import com.sobrenaturaldirector.planner.requirement.PlanRequirement;
import com.sobrenaturaldirector.content.model.*;

public final class PlanRequirementResolution {
    private PlanRequirementResolution() { }
    public static ResolutionRequest requestFor(PlanRequirement requirement) {
        if(requirement==null)throw new IllegalArgumentException("requirement");
        return new ResolutionRequest(Collections.singleton(new SemanticCapability(capabilityFor(requirement.getType()))),Collections.<SemanticCapability>emptySet(),Collections.<ContentKind>emptySet(),Collections.<ProviderId>emptyList(),Collections.<ProviderId>emptySet(),EvidenceStatus.STATIC_CANDIDATE,ResolutionRequest.AdapterPolicy.ALLOW,ResolutionRequest.FallbackPolicy.NONE);
    }
    private static String capabilityFor(String type){
        if("LOCATION:UNDERGROUND".equals(type))return "structure:underground";
        if("STRUCTURE_THEME:CEMETERY".equals(type))return "theme:cemetery";
        if("STRUCTURE_TYPE:CATACOMB".equals(type))return "structure:catacomb";
        if("CONTENT_ROLE:COMMON_HOSTILE".equals(type))return "content:common_hostile";
        if("CONTENT_ROLE:BOSS".equals(type))return "content:boss";
        if("CONTENT_ROLE:HIGH_VALUE_REWARD".equals(type))return "content:high_value_reward";
        if("SAFETY:AVOID_PLAYER_BUILD".equals(type))return "safety:avoid_player_build";
        throw new IllegalArgumentException("unsupported planner requirement: "+type);
    }
}
