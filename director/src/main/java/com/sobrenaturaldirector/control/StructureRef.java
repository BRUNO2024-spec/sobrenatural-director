package com.sobrenaturaldirector.control;

import com.sobrenaturaldirector.domain.StableId;

public final class StructureRef {
    private final String id; private final WorldPositionRef anchor; private final StructureProvenance provenance;
    public StructureRef(String id, WorldPositionRef anchor, StructureProvenance provenance){this.id=StableId.require(id,"structureId");if(anchor==null||provenance==null)throw new IllegalArgumentException("invalid structure reference");this.anchor=anchor;this.provenance=provenance;}
    public String getId(){return id;} public WorldPositionRef getAnchor(){return anchor;} public StructureProvenance getProvenance(){return provenance;}
}
