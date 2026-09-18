package com.sobrenaturaldirector.control;

import com.sobrenaturaldirector.domain.StableId;

public final class ItemRef {
    private final String semanticId; private final ItemProvenance provenance;
    public ItemRef(String id, ItemProvenance provenance){semanticId=StableId.require(id,"itemId");if(provenance==null)throw new IllegalArgumentException("provenance");this.provenance=provenance;}
    public String getSemanticId(){return semanticId;} public ItemProvenance getProvenance(){return provenance;}
}
