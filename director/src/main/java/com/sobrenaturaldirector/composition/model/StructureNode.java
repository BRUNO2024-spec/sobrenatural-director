package com.sobrenaturaldirector.composition.model;

import java.util.*;
import com.sobrenaturaldirector.domain.StableId;

public final class StructureNode {
    private final String id;private final StructureNodeType type;private final String role,theme;private final boolean mandatory;private final LogicalPosition position;private final StructureStage stage;private final List<String> anchors;
    public StructureNode(String id,StructureNodeType type,String role,String theme,boolean mandatory,LogicalPosition position,StructureStage stage,Collection<String> anchors){this.id=StableId.require(id,"nodeId");if(type==null||position==null||stage==null)throw new IllegalArgumentException("invalid node");this.type=type;this.role=role==null?"":role;this.theme=theme==null?"":theme;this.mandatory=mandatory;this.position=position;this.stage=stage;TreeSet<String>s=new TreeSet<String>();if(anchors!=null)s.addAll(anchors);this.anchors=Collections.unmodifiableList(new ArrayList<String>(s));}
    public String getId(){return id;}public StructureNodeType getType(){return type;}public String getRole(){return role;}public String getTheme(){return theme;}public boolean isMandatory(){return mandatory;}public LogicalPosition getPosition(){return position;}public StructureStage getStage(){return stage;}public List<String> getAnchors(){return anchors;}
}
