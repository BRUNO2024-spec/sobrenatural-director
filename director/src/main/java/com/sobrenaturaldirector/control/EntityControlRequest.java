package com.sobrenaturaldirector.control;
import com.sobrenaturaldirector.content.model.*;
public final class EntityControlRequest extends ControlRequest {
    private final EntityRef target; private final EntityLifecycleOperation operation; private final String mutationId, displayName; private final WorldPositionRef position;
    public EntityControlRequest(ProviderId provider,SemanticCapability capability,EntityRef target,ControlLease lease,ControlAuthority authority){this(provider,capability,target,EntityLifecycleOperation.SPAWN,target==null?null:target.getId(),target==null?null:target.getId(),null,lease,authority);}
    public EntityControlRequest(ProviderId provider,SemanticCapability capability,EntityRef target,EntityLifecycleOperation operation,String mutationId,String displayName,ControlLease lease,ControlAuthority authority){this(provider,capability,target,operation,mutationId,displayName,null,lease,authority);}
    public EntityControlRequest(ProviderId provider,SemanticCapability capability,EntityRef target,EntityLifecycleOperation operation,String mutationId,String displayName,WorldPositionRef position,ControlLease lease,ControlAuthority authority){super(ControlDomain.ENTITY,provider,capability,target==null?null:target.getDimension(),lease,authority);if(target==null)throw new IllegalArgumentException("entity target");if(operation==null||mutationId==null||displayName==null)throw new IllegalArgumentException("lifecycle fields");if(position!=null&&!position.getDimension().equals(target.getDimension()))throw new IllegalArgumentException("position dimension mismatch");this.target=target;this.operation=operation;this.mutationId=mutationId;this.displayName=displayName;this.position=position;}
    public EntityRef getTarget(){return target;}
    public EntityLifecycleOperation getOperation(){return operation==null?EntityLifecycleOperation.SPAWN:operation;}
    public String getMutationId(){return mutationId==null?target.getId():mutationId;}
    public String getDisplayName(){return displayName==null?target.getId():displayName;}
    public WorldPositionRef getPosition(){return position;}
}
