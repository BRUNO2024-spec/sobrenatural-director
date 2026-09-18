package com.sobrenaturaldirector.control;

import com.sobrenaturaldirector.domain.StableId;

/** Opaque, non-player-identifying entity reference. */
public final class EntityRef {
    private final String id;
    private final DimensionRef dimension;
    private final EntityOwnership ownership;
    public EntityRef(String id, DimensionRef dimension, EntityOwnership ownership) {
        this.id=StableId.require(id,"entityId"); if (dimension==null||ownership==null) throw new IllegalArgumentException("invalid entity reference"); this.dimension=dimension; this.ownership=ownership;
    }
    public String getId(){return id;} public DimensionRef getDimension(){return dimension;} public EntityOwnership getOwnership(){return ownership;}
}
