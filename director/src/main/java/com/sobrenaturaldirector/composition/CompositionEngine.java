package com.sobrenaturaldirector.composition;

import java.util.*;
import com.sobrenaturaldirector.composition.grammar.StructureGrammar;
import com.sobrenaturaldirector.composition.model.*;
import com.sobrenaturaldirector.composition.validation.CompositionValidator;
import com.sobrenaturaldirector.decision.model.Intent;
import com.sobrenaturaldirector.planner.model.DirectorPlan;
import com.sobrenaturaldirector.planner.requirement.PlanRequirement;

/** Pure composition boundary. It emits logical plans and never resolves coordinates or mutates a world. */
public final class CompositionEngine {
    public CompositionResult compose(CompositionRequest request) {
        List<String> trace=new ArrayList<String>();
        if(request==null||request.getPlan()==null||request.getPlan().getIntent()==Intent.NO_ACTION){trace.add("no_action");return new CompositionResult(CompositionStatus.NO_ACTION,null,null,null,new CompositionTrace(trace));}
        DirectorPlan p=request.getPlan();boolean structure=false,boss=false,reward=false,common=false;
        PlanRequirement commonReq=null,bossReq=null,rewardReq=null;
        for(PlanRequirement r:p.getRequirements()){String t=r.getType();if(t.startsWith("LOCATION:")||t.startsWith("STRUCTURE_"))structure=true;if("CONTENT_ROLE:COMMON_HOSTILE".equals(t)){common=true;commonReq=r;}if("CONTENT_ROLE:BOSS".equals(t)){boss=true;bossReq=r;}if("CONTENT_ROLE:HIGH_VALUE_REWARD".equals(t)){reward=true;rewardReq=r;}}
        Intent i=p.getIntent();if(i==Intent.BOSS_ENCOUNTER||i==Intent.BOSS_SETUP)boss=true;if(i==Intent.MINOR_ENCOUNTER||i==Intent.AMBUSH||i==Intent.STALKING_PRESSURE)common=true;
        StructureBlueprint blueprint=null;List<EncounterBlueprint> encounters=new ArrayList<EncounterBlueprint>();List<RewardBlueprint> rewards=new ArrayList<RewardBlueprint>();boolean partial=false;
        if(structure){String sid=p.getPlanId()+":structure";blueprint=StructureGrammar.build(sid,request.getTheme(),request.getScale(),boss,reward);List<String> errors=CompositionValidator.validate(blueprint);if(!errors.isEmpty()){trace.addAll(errors);return new CompositionResult(CompositionStatus.INVALID,blueprint,null,null,new CompositionTrace(trace));}trace.add("structure:validated:"+blueprint.getNodes().size());}
        if(common){if(commonReq==null)commonReq=synthetic("content_role_common_hostile");ContentRoleBinding b=ContentRoleBinding.from("common_hostile",commonReq.getId(),request.getResolutions().get(commonReq.getId()));String anchor=blueprint==null?"logical-anchor:encounter":blueprint.getNodes().get(Math.min(2,blueprint.getNodes().size()-1)).getId();encounters.add(new EncounterBlueprint(p.getPlanId()+":encounter:common",anchor,"common_hostile",1,b));partial|=b.getStatus()!=BindingStatus.RESOLVED;trace.add("encounter:common:"+b.getStatus());}
        if(boss){if(bossReq==null)bossReq=synthetic("content_role_boss");ContentRoleBinding b=ContentRoleBinding.from("boss",bossReq.getId(),request.getResolutions().get(bossReq.getId()));encounters.add(new EncounterBlueprint(p.getPlanId()+":encounter:boss",node(blueprint,StructureNodeType.ARENA),"boss",5,b));partial|=b.getStatus()!=BindingStatus.RESOLVED;trace.add("encounter:boss:"+b.getStatus());}
        if(reward){if(rewardReq==null)rewardReq=synthetic("content_role_high_value_reward");ContentRoleBinding b=ContentRoleBinding.from("high_value_reward",rewardReq.getId(),request.getResolutions().get(rewardReq.getId()));rewards.add(new RewardBlueprint(p.getPlanId()+":reward",node(blueprint,StructureNodeType.REWARD_ROOM),"high_value_reward",b));partial|=b.getStatus()!=BindingStatus.RESOLVED;trace.add("reward:"+b.getStatus());}
        return new CompositionResult(partial?CompositionStatus.PARTIAL:CompositionStatus.COMPOSED,blueprint,encounters,rewards,new CompositionTrace(trace));
    }
    private static String node(StructureBlueprint b,StructureNodeType type){if(b!=null)for(StructureNode n:b.getNodes())if(n.getType()==type)return n.getId();return "logical-anchor:"+type.name().toLowerCase(Locale.ENGLISH);}
    private static PlanRequirement synthetic(String id){return new PlanRequirement(id,id.toUpperCase(Locale.ENGLISH),com.sobrenaturaldirector.planner.model.RequirementStatus.DEFERRED,false,null);}
}
