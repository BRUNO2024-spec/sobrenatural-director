package com.sobrenaturaldirector.action;

import java.util.*;
import com.sobrenaturaldirector.control.*;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;

/** Deterministic, opt-in executor. It has no provider/mod-specific branches. */
public final class ActionPlanExecutor {
    private final DirectorProviderRegistry registry; private final SemanticActionCatalog catalog; private final ActionExecutionBindingRegistry bindings; private final ActionPlanValidator validator; private final Map<String,ActionPlanJournal> journals=new HashMap<String,ActionPlanJournal>();
    public ActionPlanExecutor(DirectorProviderRegistry registry,SemanticActionCatalog catalog,ActionExecutionBindingRegistry bindings){if(registry==null||catalog==null||bindings==null)throw new IllegalArgumentException("executor dependencies");this.registry=registry;this.catalog=catalog;this.bindings=bindings;this.validator=new ActionPlanValidator();}
    public synchronized ActionOutcome execute(ActionPlan plan,Map<String,ActionExecutionSpec> specs,ControlExecutionContext context,long tick){
        if(plan==null||specs==null||context==null||tick<0)return new ActionOutcome(plan==null?"invalid":plan.getPlanId(),ActionExecutionStatus.ABORTED,0,0,"invalid execution input");
        ActionPlanValidation valid=validator.validate(plan,catalog);if(!valid.isValid())return new ActionOutcome(plan.getPlanId(),plan.getDimension().isAvailable()?ActionExecutionStatus.STALE:ActionExecutionStatus.STALE,0,0,valid.getReason());
        if(plan.getDimension().getDimensionId()!=context.getWorld().provider.dimensionId)return new ActionOutcome(plan.getPlanId(),ActionExecutionStatus.STALE,0,0,"execution dimension mismatch");
        ActionPlanJournal journal=journals.get(plan.getPlanId());if(journal==null){journal=new ActionPlanJournal(plan.getPlanId(),plan.getFingerprint());journals.put(plan.getPlanId(),journal);}else if(!journal.getPlanFingerprint().equals(plan.getFingerprint()))return new ActionOutcome(plan.getPlanId(),ActionExecutionStatus.STALE,0,0,"plan fingerprint changed");
        if(!journal.begin())return new ActionOutcome(plan.getPlanId(),ActionExecutionStatus.ABORTED,0,0,"duplicate execution");
        List<ActionPlanNode> ordered=topological(plan.getNodes());List<ActionPlanNode> applied=new ArrayList<ActionPlanNode>();int completed=0,failed=0,compensated=0;ActionExecutionStatus failureStatus=ActionExecutionStatus.FAILED;
        for(ActionPlanNode node:ordered){ActionDefinition definition=catalog.get(node.getActionId());ActionExecutionBinding binding=definition==null?null:bindings.get(definition.getType());ActionExecutionSpec spec=specs.get(node.getNodeId());
            if(binding==null||(spec==null&&definition.getType()!=ActionType.NO_ACTION)||(spec!=null&&!binding.supports(spec))){if(node.isOptional())continue;failed++;journal.record(new ActionPlanJournal.Entry(node.getNodeId(),node.getActionId(),binding==null?"":"BOUND","",ControlResultStatus.INVALID_REQUEST,ActionExecutionStatus.COMPENSATION_UNAVAILABLE));break;}
            ControlRequest request;try{request=binding.request(spec,registry,tick);}catch(RuntimeException e){failed++;failureStatus=ActionExecutionStatus.PROVIDER_UNAVAILABLE;journal.record(new ActionPlanJournal.Entry(node.getNodeId(),node.getActionId(),binding.getClass().getName(),"",ControlResultStatus.PROVIDER_UNAVAILABLE,ActionExecutionStatus.COMPENSATION_UNAVAILABLE,e.getMessage()));break;}
            ControlResult result=request==null?new ControlResult(ControlResultStatus.APPLIED,"NO_ACTION"):new ConcreteControlRouter(registry).execute(request,context,tick);journal.record(new ActionPlanJournal.Entry(node.getNodeId(),node.getActionId(),binding.getClass().getName(),request==null?"":request.getProvider().getValue(),result.getStatus(),ActionExecutionStatus.NO_COMPENSATION_NEEDED,result.getReason()));
            if(result.getStatus()==ControlResultStatus.APPLIED){completed++;applied.add(node);}else if(node.isOptional())continue;else{failed++;if(result.getStatus()==ControlResultStatus.STALE_TARGET)failureStatus=ActionExecutionStatus.STALE;else if(result.getStatus()==ControlResultStatus.PROVIDER_UNAVAILABLE)failureStatus=ActionExecutionStatus.PROVIDER_UNAVAILABLE;else if(result.getStatus()==ControlResultStatus.SAFETY_REJECTED)failureStatus=ActionExecutionStatus.SAFETY_REJECTED;break;}
        }
        if(failed==0){journal.status(ActionExecutionStatus.COMPLETED);return new ActionOutcome(plan.getPlanId(),ActionExecutionStatus.COMPLETED,completed,0,0,"all required steps applied");}
        journal.status(ActionExecutionStatus.COMPENSATING);for(int i=applied.size()-1;i>=0;i--){ActionPlanNode node=applied.get(i);ActionExecutionSpec spec=specs.get(node.getNodeId());ActionExecutionBinding binding=bindings.get(catalog.get(node.getActionId()).getType());ControlRequest compensation=binding==null?null:binding.compensation(spec,registry,tick);if(compensation==null)continue;ControlResult result=new ConcreteControlRouter(registry).execute(compensation,context,tick);if(result.getStatus()==ControlResultStatus.APPLIED)compensated++;}
        ActionExecutionStatus terminal;
        if(applied.isEmpty()) terminal=failureStatus;
        else terminal=compensated==applied.size()?ActionExecutionStatus.COMPENSATED:ActionExecutionStatus.FAILED_COMPENSATION;
        journal.status(terminal);return new ActionOutcome(plan.getPlanId(),terminal,completed,failed,compensated,"required step failed");
    }
    public ActionPlanJournal journal(String planId){return journals.get(planId);}
    private static List<ActionPlanNode> topological(List<ActionPlanNode> nodes){Map<String,ActionPlanNode> byId=new TreeMap<String,ActionPlanNode>();for(ActionPlanNode node:nodes)byId.put(node.getNodeId(),node);List<ActionPlanNode> result=new ArrayList<ActionPlanNode>();Set<String> done=new HashSet<String>();while(result.size()<nodes.size()){boolean progress=false;for(ActionPlanNode node:byId.values())if(!done.contains(node.getNodeId())&&done.containsAll(node.getDependencies())){done.add(node.getNodeId());result.add(node);progress=true;}if(!progress)throw new IllegalArgumentException("cyclic action plan");}return result;}
}
