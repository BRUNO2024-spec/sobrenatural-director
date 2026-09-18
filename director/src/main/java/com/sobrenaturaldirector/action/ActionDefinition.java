package com.sobrenaturaldirector.action;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;
import com.sobrenaturaldirector.content.model.*;
import com.sobrenaturaldirector.decision.model.ActionSafety;

/** Immutable semantic action metadata. It contains no live runtime object. */
public final class ActionDefinition implements Comparable<ActionDefinition> {
    private final String id; private final ActionType type; private final ActionFamily family; private final ActionKind kind;
    private final boolean executable; private final Set<SemanticCapability> capabilities; private final Set<ContentKind> contentKinds;
    private final ActionTargetScope dimensionScope; private final ActionRollbackPolicy rollback; private final ActionSafety risk;
    private final Map<String,Double> costs; private final String providerPath, unavailableReason;
    public ActionDefinition(String id,ActionType type,ActionFamily family,ActionKind kind,boolean executable,Collection<SemanticCapability> capabilities,Collection<ContentKind> contentKinds,ActionTargetScope dimensionScope,ActionRollbackPolicy rollback,ActionSafety risk,Map<String,Double> costs,String providerPath,String unavailableReason){
        if(id==null||id.length()==0||type==null||family==null||kind==null||dimensionScope==null||rollback==null||risk==null)throw new IllegalArgumentException("invalid action definition");
        this.id=id;this.type=type;this.family=family;this.kind=kind;this.executable=executable;this.capabilities=sortedCapabilities(capabilities);this.contentKinds=sortedKinds(contentKinds);this.dimensionScope=dimensionScope;this.rollback=rollback;this.risk=risk;this.costs=sortedCosts(costs);this.providerPath=providerPath==null?"":providerPath;this.unavailableReason=unavailableReason==null?"":unavailableReason;
    }
    private static Set<SemanticCapability> sortedCapabilities(Collection<SemanticCapability> x){TreeSet<SemanticCapability> r=new TreeSet<SemanticCapability>();if(x!=null)for(SemanticCapability v:x)if(v!=null)r.add(v);return Collections.unmodifiableSet(r);}
    private static Set<ContentKind> sortedKinds(Collection<ContentKind> x){TreeSet<ContentKind> r=new TreeSet<ContentKind>(new Comparator<ContentKind>(){public int compare(ContentKind a,ContentKind b){return a.name().compareTo(b.name());}});if(x!=null)for(ContentKind v:x)if(v!=null)r.add(v);return Collections.unmodifiableSet(r);}
    private static Map<String,Double> sortedCosts(Map<String,Double> x){TreeMap<String,Double> r=new TreeMap<String,Double>();if(x!=null)for(Map.Entry<String,Double> e:x.entrySet()){if(e.getKey()==null||e.getKey().length()==0||e.getValue()==null||Double.isNaN(e.getValue())||Double.isInfinite(e.getValue())||e.getValue()<0)throw new IllegalArgumentException("invalid action cost");r.put(e.getKey(),e.getValue());}return Collections.unmodifiableMap(r);}
    public String getId(){return id;}public ActionType getType(){return type;}public ActionFamily getFamily(){return family;}public ActionKind getKind(){return kind;}public boolean isExecutable(){return executable;}public Set<SemanticCapability> getCapabilities(){return capabilities;}public Set<ContentKind> getContentKinds(){return contentKinds;}public ActionTargetScope getDimensionScope(){return dimensionScope;}public ActionRollbackPolicy getRollback(){return rollback;}public ActionSafety getRisk(){return risk;}public Map<String,Double> getCosts(){return costs;}public String getProviderPath(){return providerPath;}public String getUnavailableReason(){return unavailableReason;}
    public int compareTo(ActionDefinition o){return id.compareTo(o.id);}
    public String fingerprint(){try{MessageDigest d=MessageDigest.getInstance("SHA-256");String s=id+"|"+type+"|"+family+"|"+kind+"|"+executable+"|"+capabilities+"|"+contentKinds+"|"+dimensionScope+"|"+rollback+"|"+risk+"|"+costs+"|"+providerPath+"|"+unavailableReason;byte[] b=d.digest(s.getBytes(Charset.forName("UTF-8")));StringBuilder x=new StringBuilder();for(byte v:b)x.append(String.format(Locale.ENGLISH,"%02x",v&255));return x.toString();}catch(Exception e){throw new IllegalStateException(e);}}
}
