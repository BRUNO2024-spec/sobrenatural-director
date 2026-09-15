package com.sobrenaturaldirector.composition.model;

import com.sobrenaturaldirector.domain.StableId;

public final class RewardBlueprint {private final String id,nodeId,role;private final ContentRoleBinding binding;public RewardBlueprint(String id,String nodeId,String role,ContentRoleBinding binding){this.id=StableId.require(id,"rewardId");this.nodeId=StableId.require(nodeId,"rewardNodeId");this.role=StableId.require(role,"rewardRole");this.binding=binding;}public String getId(){return id;}public String getNodeId(){return nodeId;}public String getRole(){return role;}public ContentRoleBinding getBinding(){return binding;}}
