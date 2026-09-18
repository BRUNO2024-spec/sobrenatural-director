package com.sobrenaturaldirector.action;

import java.util.*;

public final class ActionPlanNode {
    private final String nodeId, actionId; private final List<String> dependencies; private final boolean optional; private final String failurePolicy;
    public ActionPlanNode(String nodeId,String actionId,Collection<String> dependencies,boolean optional,String failurePolicy){if(nodeId==null||nodeId.length()==0||actionId==null||actionId.length()==0)throw new IllegalArgumentException("invalid action node");this.nodeId=nodeId;this.actionId=actionId;TreeSet<String>s=new TreeSet<String>();if(dependencies!=null)s.addAll(dependencies);if(s.contains(nodeId))throw new IllegalArgumentException("self dependency");this.dependencies=Collections.unmodifiableList(new ArrayList<String>(s));this.optional=optional;this.failurePolicy=failurePolicy==null?"ABORT":""+failurePolicy;}
    public String getNodeId(){return nodeId;}public String getActionId(){return actionId;}public List<String> getDependencies(){return dependencies;}public boolean isOptional(){return optional;}public String getFailurePolicy(){return failurePolicy;}
}
