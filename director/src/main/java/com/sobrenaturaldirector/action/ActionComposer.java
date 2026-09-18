package com.sobrenaturaldirector.action;

import java.util.*;
import com.sobrenaturaldirector.control.DimensionRef;

/** Deterministic plan composer. No provider or world call occurs here. */
public final class ActionComposer {
    public static final String COMPOSITION_VERSION="action-composition-v1";
    public ActionPlan compose(String planId,ActionType type,DimensionRef dimension,SemanticActionCatalog catalog){if(type==null||dimension==null||catalog==null)throw new IllegalArgumentException("composition inputs");List<ActionPlanNode> nodes=new ArrayList<ActionPlanNode>();if(type==ActionType.CREATE_INVESTIGATION_SITE){nodes.add(new ActionPlanNode("structure","PLACE_STRUCTURE",Collections.<String>emptyList(),false,"COMPENSATE"));nodes.add(new ActionPlanNode("witness","SPAWN_ACTOR",Collections.singleton("structure"),true,"COMPENSATE"));}else if(type==ActionType.CREATE_THREAT_PRESENCE){nodes.add(new ActionPlanNode("threat","SPAWN_THREAT",Collections.<String>emptyList(),false,"COMPENSATE"));}else{ActionDefinition d=find(type,catalog);if(d==null)throw new IllegalArgumentException("action type unavailable");nodes.add(new ActionPlanNode("action",d.getId(),Collections.<String>emptyList(),false,"ABORT"));}ActionPlan plan=new ActionPlan(planId,dimension,nodes);ActionPlanValidation v=new ActionPlanValidator().validate(plan,catalog);if(!v.isValid())throw new IllegalArgumentException(v.getReason());return plan;}
    private ActionDefinition find(ActionType type,SemanticActionCatalog c){for(ActionDefinition d:c.getDefinitions().values())if(d.getType()==type)return d;return null;}
}
