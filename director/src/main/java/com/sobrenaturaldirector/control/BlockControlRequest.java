package com.sobrenaturaldirector.control;
import com.sobrenaturaldirector.content.model.*;
public final class BlockControlRequest extends ControlRequest {
    public enum Operation { PLACE, REMOVE, REPLACE }
    private final WorldPositionRef target; private final Operation operation; private final BlockProvenance provenance;
    public BlockControlRequest(ProviderId provider,SemanticCapability capability,WorldPositionRef target,Operation operation,BlockProvenance provenance,ControlLease lease,ControlAuthority authority){super(ControlDomain.BLOCK,provider,capability,target==null?null:target.getDimension(),lease,authority);if(target==null||operation==null||provenance==null)throw new IllegalArgumentException("invalid block request");this.target=target;this.operation=operation;this.provenance=provenance;}
    public WorldPositionRef getTarget(){return target;} public Operation getOperation(){return operation;} public BlockProvenance getProvenance(){return provenance;}
}
