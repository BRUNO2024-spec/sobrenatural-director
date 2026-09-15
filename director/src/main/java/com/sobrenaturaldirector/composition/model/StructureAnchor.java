package com.sobrenaturaldirector.composition.model;

import java.util.*;
import com.sobrenaturaldirector.domain.StableId;
import com.sobrenaturaldirector.content.model.SemanticCapability;

public final class StructureAnchor {private final String id,nodeId;private final boolean mandatory;private final Set<SemanticCapability> requirements;public StructureAnchor(String id,String nodeId,boolean mandatory,Collection<SemanticCapability> requirements){this.id=StableId.require(id,"anchorId");this.nodeId=StableId.require(nodeId,"anchorNodeId");this.mandatory=mandatory;TreeSet<SemanticCapability>s=new TreeSet<SemanticCapability>();if(requirements!=null)s.addAll(requirements);this.requirements=Collections.unmodifiableSet(s);}public String getId(){return id;}public String getNodeId(){return nodeId;}public boolean isMandatory(){return mandatory;}public Set<SemanticCapability> getRequirements(){return requirements;}}
