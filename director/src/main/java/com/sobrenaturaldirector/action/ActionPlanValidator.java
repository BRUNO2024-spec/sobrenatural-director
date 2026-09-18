package com.sobrenaturaldirector.action;

import java.util.*;

public final class ActionPlanValidator {
    public ActionPlanValidation validate(ActionPlan plan,SemanticActionCatalog catalog){if(plan==null||catalog==null)return new ActionPlanValidation(false,"missing plan or catalog");if(!plan.getDimension().isAvailable())return new ActionPlanValidation(false,"target dimension unavailable");Map<String,ActionPlanNode> nodes=new HashMap<String,ActionPlanNode>();for(ActionPlanNode n:plan.getNodes()){ActionDefinition definition=catalog.get(n.getActionId());if(definition==null)return new ActionPlanValidation(false,"unknown action "+n.getActionId());if(!definition.isExecutable()&&!n.isOptional())return new ActionPlanValidation(false,"action unavailable "+n.getActionId());nodes.put(n.getNodeId(),n);}Map<String,Integer> state=new HashMap<String,Integer>();for(String id:nodes.keySet())if(cycle(id,nodes,state))return new ActionPlanValidation(false,"cycle or missing dependency at "+id);return new ActionPlanValidation(true,"valid");}
    private boolean cycle(String id,Map<String,ActionPlanNode> nodes,Map<String,Integer> state){Integer s=state.get(id);if(s!=null)return s==1;if(!nodes.containsKey(id))return true;state.put(id,1);for(String dep:nodes.get(id).getDependencies())if(cycle(dep,nodes,state))return true;state.put(id,2);return false;}
}
