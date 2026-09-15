package com.sobrenaturaldirector.composition.model;

import com.sobrenaturaldirector.domain.StableId;

public final class StructureConnector {private final String id,from,to,direction;private final StructureConnectorType type;private final boolean mandatory;public StructureConnector(String id,String from,String to,StructureConnectorType type,boolean mandatory,String direction){this.id=StableId.require(id,"connectorId");this.from=StableId.require(from,"fromNode");this.to=StableId.require(to,"toNode");if(type==null)throw new IllegalArgumentException("connector type");this.type=type;this.mandatory=mandatory;this.direction=direction==null?"":direction;}public String getId(){return id;}public String getFrom(){return from;}public String getTo(){return to;}public StructureConnectorType getType(){return type;}public boolean isMandatory(){return mandatory;}public String getDirection(){return direction;}}
