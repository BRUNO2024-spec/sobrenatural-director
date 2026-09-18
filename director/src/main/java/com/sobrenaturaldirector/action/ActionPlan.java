package com.sobrenaturaldirector.action;

import java.security.MessageDigest;import java.nio.charset.Charset;import java.util.*;
import com.sobrenaturaldirector.control.DimensionRef;

/** Immutable bounded action DAG; it is a plan, never an execution. */
public final class ActionPlan {
    public static final int MAX_NODES=32;
    private final String planId; private final DimensionRef dimension; private final List<ActionPlanNode> nodes; private final String fingerprint;
    public ActionPlan(String planId,DimensionRef dimension,Collection<ActionPlanNode> nodes){if(planId==null||planId.length()==0||dimension==null||nodes==null||nodes.isEmpty()||nodes.size()>MAX_NODES)throw new IllegalArgumentException("invalid action plan");this.planId=planId;this.dimension=dimension;List<ActionPlanNode> copy=new ArrayList<ActionPlanNode>(nodes);Collections.sort(copy,new Comparator<ActionPlanNode>(){public int compare(ActionPlanNode a,ActionPlanNode b){return a.getNodeId().compareTo(b.getNodeId());}});Set<String> ids=new HashSet<String>();for(ActionPlanNode n:copy)if(!ids.add(n.getNodeId()))throw new IllegalArgumentException("duplicate action node");this.nodes=Collections.unmodifiableList(copy);this.fingerprint=fingerprint(copy);}
    private String fingerprint(List<ActionPlanNode> nodes){try{MessageDigest d=MessageDigest.getInstance("SHA-256");d.update((dimension.getDimensionId()+"|"+dimension.getProviderIdentity()+"|"+dimension.getTags()+"|"+dimension.isAvailable()+"\n").getBytes(Charset.forName("UTF-8")));for(ActionPlanNode n:nodes)d.update((n.getNodeId()+"|"+n.getActionId()+"|"+n.getDependencies()+"|"+n.isOptional()+"|"+n.getFailurePolicy()+"\n").getBytes(Charset.forName("UTF-8")));byte[] b=d.digest();StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format(Locale.ENGLISH,"%02x",x&255));return s.toString();}catch(Exception e){throw new IllegalStateException(e);}}
    public String getPlanId(){return planId;}public DimensionRef getDimension(){return dimension;}public List<ActionPlanNode> getNodes(){return nodes;}public String getFingerprint(){return fingerprint;}
}
