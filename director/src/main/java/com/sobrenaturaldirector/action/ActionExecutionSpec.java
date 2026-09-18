package com.sobrenaturaldirector.action;

import com.sobrenaturaldirector.composition.model.DirectorOwnedCompositionPlan;
import com.sobrenaturaldirector.control.*;

/** Immutable execution input; it carries refs only, never live world/entity objects. */
public final class ActionExecutionSpec {
    private final EntityRef entity; private final WorldPositionRef position;
    private final DirectorOwnedCompositionPlan composition; private final ControlLease lease;
    private final ControlAuthority authority; private final String mutationId, displayName;
    public ActionExecutionSpec(EntityRef entity, WorldPositionRef position,
            DirectorOwnedCompositionPlan composition, ControlLease lease,
            ControlAuthority authority, String mutationId, String displayName) {
        this.entity=entity; this.position=position; this.composition=composition; this.lease=lease;
        this.authority=authority; this.mutationId=mutationId; this.displayName=displayName;
    }
    public EntityRef getEntity(){return entity;} public WorldPositionRef getPosition(){return position;}
    public DirectorOwnedCompositionPlan getComposition(){return composition;} public ControlLease getLease(){return lease;}
    public ControlAuthority getAuthority(){return authority;} public String getMutationId(){return mutationId;}
    public String getDisplayName(){return displayName;}
}
