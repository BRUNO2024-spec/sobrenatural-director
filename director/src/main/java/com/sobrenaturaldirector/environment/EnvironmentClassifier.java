package com.sobrenaturaldirector.environment;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.sobrenaturaldirector.environment.model.EnvironmentClassification;
import com.sobrenaturaldirector.environment.model.EnvironmentEvidence;
import com.sobrenaturaldirector.environment.model.EnvironmentSignal;
import com.sobrenaturaldirector.environment.model.SemanticRegionProfile;

/** Deterministic, explainable aggregation. Counts are capped by the scanner budget. */
public final class EnvironmentClassifier {
    public SemanticRegionProfile classify(EnvironmentEvidence e){
        int light=e.count(EnvironmentSignal.LIGHT_SOURCE), utility=e.count(EnvironmentSignal.STORAGE)+e.count(EnvironmentSignal.CRAFTING)+e.count(EnvironmentSignal.SMELTING), fort=e.count(EnvironmentSignal.FORTIFICATION)+e.count(EnvironmentSignal.ACCESS_CONTROL), living=e.count(EnvironmentSignal.SLEEPING)+utility;
        int modification=e.count(EnvironmentSignal.PLAYER_MODIFICATION)+light+utility+fort+e.count(EnvironmentSignal.FARMING);
        int activity=activity(e.getTick(),e.getLastActivityTick());
        int score=Math.min(100, modification*5+Math.min(25,activity/4)+Math.min(20,e.getPlayers()*10));
        EnvironmentClassification c;
        if(e.isVillage()) c=EnvironmentClassification.VANILLA_VILLAGE;
        else if(score>=50&&living>=4&&activity>=20) c=EnvironmentClassification.ESTABLISHED_PLAYER_AREA;
        else if(score>=25&&living>=2) c=EnvironmentClassification.TEMPORARY_CAMP;
        else if(modification>0) c=EnvironmentClassification.PLAYER_MODIFIED_AREA;
        else c=EnvironmentClassification.WILDERNESS;
        if(e.isDirectorOwned()&&c==EnvironmentClassification.ESTABLISHED_PLAYER_AREA){score=Math.max(0,score-35);c=modification>0?EnvironmentClassification.PLAYER_MODIFIED_AREA:EnvironmentClassification.UNKNOWN;}
        Set<EnvironmentSignal> tags=EnumSet.noneOf(EnvironmentSignal.class);for(EnvironmentSignal s:EnvironmentSignal.values())if(e.count(s)>0)tags.add(s);if(e.isVillage())tags.add(EnvironmentSignal.VANILLA_VILLAGE);if(activity>0)tags.add(EnvironmentSignal.RECENT_ACTIVITY);
        List<String> reasons=new ArrayList<String>();if(light>0)reasons.add("ARTIFICIAL_LIGHTING");if(utility>0)reasons.add("LIVING_UTILITIES");if(fort>0)reasons.add("FORTIFICATION");if(activity>0)reasons.add("RECENT_PLAYER_ACTIVITY");if(e.isVillage())reasons.add("VANILLA_VILLAGE");if(e.isDirectorOwned())reasons.add("DIRECTOR_OWNED_DISCOUNT");
        return new SemanticRegionProfile(e,c,score,activity,tags,reasons);
    }
    public static int activity(long now,long last){if(last<=0||now<last)return 0;long age=now-last;return age>=12000?0:(int)Math.max(0,100-(age*100/12000));}
}
