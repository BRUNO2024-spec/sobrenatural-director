package com.sobrenaturaldirector.control;
import com.sobrenaturaldirector.content.model.*;
public final class EntityControlRequest extends ControlRequest {
    private final EntityRef target;
    public EntityControlRequest(ProviderId provider,SemanticCapability capability,EntityRef target,ControlLease lease,ControlAuthority authority){super(ControlDomain.ENTITY,provider,capability,target==null?null:target.getDimension(),lease,authority);if(target==null)throw new IllegalArgumentException("entity target");this.target=target;}
    public EntityRef getTarget(){return target;}
}
