package com.sobrenaturaldirector.content.model;

import java.util.*;

public final class SemanticContentEntry {
    private final ContentKey key; private final ProviderId provider; private final ContentKind kind; private final Set<SemanticCapability> capabilities;
    private final EvidenceStatus evidence; private final Provenance provenance; private final boolean registryResolvable; private final boolean adapterRequired;
    private final ExecutionStatus execution; private final Set<String> sources;
    public SemanticContentEntry(ContentKey key,ProviderId provider,ContentKind kind,Collection<SemanticCapability> caps,EvidenceStatus evidence,Provenance provenance,boolean registryResolvable,boolean adapterRequired,ExecutionStatus execution,Collection<String> sources) {
        if(key==null||provider==null||kind==null||evidence==null||provenance==null||execution==null)throw new IllegalArgumentException("invalid content entry");
        this.key=key;this.provider=provider;this.kind=kind;this.evidence=evidence;this.provenance=provenance;this.registryResolvable=registryResolvable;this.adapterRequired=adapterRequired;this.execution=execution;
        TreeSet<SemanticCapability> c=new TreeSet<SemanticCapability>();if(caps!=null)c.addAll(caps);this.capabilities=Collections.unmodifiableSet(c);TreeSet<String>s=new TreeSet<String>();if(sources!=null)s.addAll(sources);this.sources=Collections.unmodifiableSet(s);
    }
    public ContentKey getKey(){return key;} public ProviderId getProvider(){return provider;} public ContentKind getKind(){return kind;} public Set<SemanticCapability> getCapabilities(){return capabilities;} public EvidenceStatus getEvidence(){return evidence;} public Provenance getProvenance(){return provenance;} public boolean isRegistryResolvable(){return registryResolvable;} public boolean isAdapterRequired(){return adapterRequired;} public ExecutionStatus getExecution(){return execution;} public Set<String> getSources(){return sources;}
}
