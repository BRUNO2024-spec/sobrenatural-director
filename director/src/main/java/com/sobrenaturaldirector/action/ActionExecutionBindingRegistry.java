package com.sobrenaturaldirector.action;

import java.util.*;
import com.sobrenaturaldirector.capability.CapabilityVocabulary;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.control.*;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;

/** Deterministic binding table. It contains no external-mod type or mod branch. */
public final class ActionExecutionBindingRegistry {
    private final Map<ActionType,ActionExecutionBinding> bindings;
    public ActionExecutionBindingRegistry(Collection<ActionExecutionBinding> values) {
        TreeMap<ActionType,ActionExecutionBinding> copy=new TreeMap<ActionType,ActionExecutionBinding>(new Comparator<ActionType>(){public int compare(ActionType a,ActionType b){return a.name().compareTo(b.name());}});
        if(values!=null) for(ActionExecutionBinding value:values) if(value!=null && copy.put(value.getActionType(),value)!=null) throw new IllegalArgumentException("duplicate action binding");
        bindings=Collections.unmodifiableMap(copy);
    }
    public static ActionExecutionBindingRegistry standard(){List<ActionExecutionBinding> values=new ArrayList<ActionExecutionBinding>();
        values.add(new NoOpBinding());
        values.add(new EntityBinding(ActionType.SPAWN_ACTOR,CapabilityVocabulary.ACTOR_SOURCE,CapabilityVocabulary.ENTITY_LIFECYCLE_CONTROL,EntityLifecycleOperation.SPAWN));
        values.add(new EntityBinding(ActionType.DESPAWN_ACTOR,CapabilityVocabulary.ACTOR_SOURCE,CapabilityVocabulary.ENTITY_LIFECYCLE_CONTROL,EntityLifecycleOperation.DESPAWN));
        values.add(new EntityBinding(ActionType.SPAWN_THREAT,CapabilityVocabulary.THREAT_SOURCE,CapabilityVocabulary.ENTITY_LIFECYCLE_CONTROL,EntityLifecycleOperation.SPAWN));
        values.add(new EntityBinding(ActionType.DESPAWN_THREAT,CapabilityVocabulary.THREAT_SOURCE,CapabilityVocabulary.ENTITY_LIFECYCLE_CONTROL,EntityLifecycleOperation.DESPAWN));
        values.add(new StructureBinding(ActionType.PLACE_STRUCTURE,CapabilityVocabulary.STRUCTURE_SOURCE,StructureControlRequest.Operation.APPLY));
        values.add(new StructureBinding(ActionType.ROLLBACK_STRUCTURE,CapabilityVocabulary.ROLLBACK_SAFE,StructureControlRequest.Operation.ROLLBACK));
        return new ActionExecutionBindingRegistry(values);
    }
    public ActionExecutionBinding get(ActionType type){return bindings.get(type);}
    public int boundExecutableCount(SemanticActionCatalog catalog){int count=0;for(ActionDefinition d:catalog.getDefinitions().values())if(d.isExecutable()&&get(d.getType())!=null)count++;return count;}
    public int unboundExecutableCount(SemanticActionCatalog catalog){int count=0;for(ActionDefinition d:catalog.getDefinitions().values())if(d.isExecutable()&&get(d.getType())==null)count++;return count;}
    public Map<ActionType,ActionExecutionBinding> getBindings(){return bindings;}

    private static final class EntityBinding implements ActionExecutionBinding {
        private final ActionType type; private final com.sobrenaturaldirector.content.model.SemanticCapability capability, lifecycle; private final EntityLifecycleOperation operation;
        EntityBinding(ActionType type,com.sobrenaturaldirector.content.model.SemanticCapability capability,com.sobrenaturaldirector.content.model.SemanticCapability lifecycle,EntityLifecycleOperation operation){this.type=type;this.capability=capability;this.lifecycle=lifecycle;this.operation=operation;}
        public ActionType getActionType(){return type;}
        public boolean supports(ActionExecutionSpec spec){return spec!=null&&spec.getEntity()!=null&&spec.getPosition()!=null&&spec.getLease()!=null&&spec.getAuthority()!=null;}
        public ControlRequest request(ActionExecutionSpec spec,DirectorProviderRegistry registry,long tick){ProviderId provider=resolvePair(registry,capability,lifecycle);return new EntityControlRequest(provider,capability,spec.getEntity(),operation,spec.getMutationId(),spec.getDisplayName(),spec.getPosition(),spec.getLease(),spec.getAuthority());}
        public ControlRequest compensation(ActionExecutionSpec spec,DirectorProviderRegistry registry,long tick){if(operation!=EntityLifecycleOperation.SPAWN)return null;ProviderId provider=resolvePair(registry,capability,lifecycle);return new EntityControlRequest(provider,capability,spec.getEntity(),EntityLifecycleOperation.DESPAWN,spec.getMutationId(),spec.getDisplayName(),spec.getPosition(),spec.getLease(),spec.getAuthority());}
    }
    private static final class NoOpBinding implements ActionExecutionBinding {
        public ActionType getActionType(){return ActionType.NO_ACTION;}
        public boolean supports(ActionExecutionSpec spec){return true;}
        public ControlRequest request(ActionExecutionSpec spec,DirectorProviderRegistry registry,long tick){return null;}
        public ControlRequest compensation(ActionExecutionSpec spec,DirectorProviderRegistry registry,long tick){return null;}
    }
    private static final class StructureBinding implements ActionExecutionBinding {
        private final ActionType type; private final com.sobrenaturaldirector.content.model.SemanticCapability capability; private final StructureControlRequest.Operation operation;
        StructureBinding(ActionType type,com.sobrenaturaldirector.content.model.SemanticCapability capability,StructureControlRequest.Operation operation){this.type=type;this.capability=capability;this.operation=operation;}
        public ActionType getActionType(){return type;}
        public boolean supports(ActionExecutionSpec spec){return spec!=null&&spec.getComposition()!=null&&spec.getLease()!=null&&spec.getAuthority()!=null;}
        public ControlRequest request(ActionExecutionSpec spec,DirectorProviderRegistry registry,long tick){ProviderId provider=resolve(registry,capability);return new StructureControlRequest(provider,capability,spec.getComposition(),operation,new DimensionRef(spec.getComposition().getDimension()),spec.getLease(),spec.getAuthority());}
        public ControlRequest compensation(ActionExecutionSpec spec,DirectorProviderRegistry registry,long tick){if(operation!=StructureControlRequest.Operation.APPLY)return null;ProviderId provider=resolve(registry,CapabilityVocabulary.ROLLBACK_SAFE);return new StructureControlRequest(provider,CapabilityVocabulary.ROLLBACK_SAFE,spec.getComposition(),StructureControlRequest.Operation.ROLLBACK,new DimensionRef(spec.getComposition().getDimension()),spec.getLease(),spec.getAuthority());}
    }
    private static ProviderId resolve(DirectorProviderRegistry registry,com.sobrenaturaldirector.content.model.SemanticCapability capability){if(registry==null)throw new IllegalArgumentException("registry");java.util.List<com.sobrenaturaldirector.capability.CapabilityDescriptor> found=registry.query(new com.sobrenaturaldirector.capability.CapabilityQuery(capability));if(found.isEmpty())throw new IllegalStateException("no provider for "+capability.getValue());return found.get(0).getProvider();}
    private static ProviderId resolvePair(DirectorProviderRegistry registry,com.sobrenaturaldirector.content.model.SemanticCapability first,com.sobrenaturaldirector.content.model.SemanticCapability second){ProviderId provider=resolve(registry,first);for(com.sobrenaturaldirector.capability.CapabilityDescriptor candidate:registry.query(new com.sobrenaturaldirector.capability.CapabilityQuery(second)))if(provider.equals(candidate.getProvider()))return provider;throw new IllegalStateException("provider lacks paired capability "+second.getValue());}
}
