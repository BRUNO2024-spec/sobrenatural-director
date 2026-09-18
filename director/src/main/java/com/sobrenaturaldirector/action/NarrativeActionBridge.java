package com.sobrenaturaldirector.action;

import java.util.*;
import com.sobrenaturaldirector.control.DimensionRef;
import com.sobrenaturaldirector.situation.*;

/** Maps narrative meaning to shared action intents without provider branches. */
public final class NarrativeActionBridge {
    public SemanticActionIntent fromGoal(SituationGoal goal,DimensionRef dimension){if(goal==null||dimension==null)throw new IllegalArgumentException("goal/dimension");if(goal==SituationGoal.INVESTIGATION)return new SemanticActionIntent(ActionType.CREATE_INVESTIGATION_SITE,"goal:"+goal.name(),dimension,Collections.singleton("INVESTIGATION"));if(goal==SituationGoal.THREAT_EVENT)return new SemanticActionIntent(ActionType.CREATE_THREAT_PRESENCE,"goal:"+goal.name(),dimension,Collections.singleton("THREAT"));if(goal==SituationGoal.DISCOVERY)return new SemanticActionIntent(ActionType.PLACE_STRUCTURE,"goal:"+goal.name(),dimension,Collections.singleton("POINT_OF_INTEREST"));return new SemanticActionIntent(ActionType.NO_ACTION,"goal:"+goal.name(),dimension,Collections.singleton("AMBIENT_EVENT"));}
    public SemanticActionIntent fromBlueprint(SituationBlueprint blueprint,DimensionRef dimension){if(blueprint==null||dimension==null)throw new IllegalArgumentException("blueprint/dimension");List<String> roles=new ArrayList<String>();for(SemanticRole role:blueprint.getRequiredRoles())roles.add(role.name());for(SemanticRole role:blueprint.getOptionalRoles())roles.add("optional:"+role.name());SemanticActionIntent base=fromGoal(blueprint.getGoal(),dimension);return new SemanticActionIntent(base.getType(),"blueprint:"+blueprint.getId(),dimension,roles);}
}
