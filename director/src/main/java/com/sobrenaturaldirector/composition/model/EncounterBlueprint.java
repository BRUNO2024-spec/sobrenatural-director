package com.sobrenaturaldirector.composition.model;

import com.sobrenaturaldirector.domain.StableId;

public final class EncounterBlueprint {private final String id,nodeId,role;private final int budget;private final ContentRoleBinding binding;public EncounterBlueprint(String id,String nodeId,String role,int budget,ContentRoleBinding binding){this.id=StableId.require(id,"encounterId");this.nodeId=StableId.require(nodeId,"encounterNodeId");this.role=StableId.require(role,"encounterRole");if(budget<0)throw new IllegalArgumentException("negative encounter budget");this.budget=budget;this.binding=binding;}public String getId(){return id;}public String getNodeId(){return nodeId;}public String getRole(){return role;}public int getBudget(){return budget;}public ContentRoleBinding getBinding(){return binding;}}
