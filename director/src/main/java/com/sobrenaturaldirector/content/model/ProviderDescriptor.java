package com.sobrenaturaldirector.content.model;

import java.util.*;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.domain.StableId;
import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CapabilityPolicyBinding;
import com.sobrenaturaldirector.capability.ProviderAvailability;

public final class ProviderDescriptor {
    private final ProviderId id; private final String label; private final EvidenceStatus evidence; private final boolean optional;
    private final ProviderStatus runtimeStatus; private final boolean adapterRequired; private final Set<String> evidenceRefs;
    private String version; private Set<CapabilityDescriptor> capabilities; private Set<CapabilityPolicyBinding> policies; private ProviderAvailability supportLevel;
    public ProviderDescriptor(ProviderId id,String label,EvidenceStatus evidence,boolean optional,ProviderStatus runtimeStatus,boolean adapterRequired,Collection<String> refs) {
        if(id==null||evidence==null||runtimeStatus==null) throw new IllegalArgumentException("invalid provider");
        this.id=id; this.label=StableId.require(label==null?id.getValue():label,"label"); this.evidence=evidence; this.optional=optional; this.runtimeStatus=runtimeStatus; this.adapterRequired=adapterRequired; this.version="unknown"; this.capabilities=Collections.emptySet(); this.policies=Collections.emptySet(); this.supportLevel=ProviderAvailability.DETECTED;
        TreeSet<String> s=new TreeSet<String>(); if(refs!=null) for(String r:refs)s.add(StableId.require(r,"evidenceRef")); this.evidenceRefs=Collections.unmodifiableSet(s);
    }
    public ProviderDescriptor(ProviderId id, String label, EvidenceStatus evidence, boolean optional, ProviderStatus runtimeStatus,
            boolean adapterRequired, Collection<String> refs, String version, Collection<CapabilityDescriptor> capabilities,
            Collection<CapabilityPolicyBinding> policies, ProviderAvailability supportLevel) {
        this(id, label, evidence, optional, runtimeStatus, adapterRequired, refs);
        this.version=StableId.require(version==null?"unknown":version,"version");
        TreeSet<CapabilityDescriptor> c=new TreeSet<CapabilityDescriptor>(new Comparator<CapabilityDescriptor>() { public int compare(CapabilityDescriptor a, CapabilityDescriptor b) { int v=a.getId().compareTo(b.getId()); return v!=0?v:a.getProvider().compareTo(b.getProvider()); } }); if(capabilities!=null)c.addAll(capabilities); this.capabilities=Collections.unmodifiableSet(c);
        this.policies=Collections.unmodifiableSet(new HashSet<CapabilityPolicyBinding>(policies==null?Collections.<CapabilityPolicyBinding>emptySet():policies));
        this.supportLevel=supportLevel==null?ProviderAvailability.DETECTED:supportLevel;
    }
    public ProviderId getId(){return id;} public String getLabel(){return label;} public EvidenceStatus getEvidence(){return evidence;} public boolean isOptional(){return optional;} public ProviderStatus getRuntimeStatus(){return runtimeStatus;} public boolean isAdapterRequired(){return adapterRequired;} public Set<String> getEvidenceRefs(){return evidenceRefs;}
    public String getVersion(){return version;} public Set<CapabilityDescriptor> getCapabilities(){return capabilities;} public Set<CapabilityPolicyBinding> getPolicies(){return policies;} public ProviderAvailability getSupportLevel(){return supportLevel;}
    public boolean isAvailable(){return runtimeStatus==ProviderStatus.AVAILABLE || runtimeStatus==ProviderStatus.AVAILABLE_SUPPORTED;}
}
