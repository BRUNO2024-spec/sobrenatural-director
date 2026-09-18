package com.sobrenaturaldirector.control;

import com.sobrenaturaldirector.content.model.SemanticCapability;
import com.sobrenaturaldirector.content.model.ProviderId;

/** Request-only contract. Providers translate it to execution later. */
public abstract class ControlRequest {
    private final ControlDomain domain; private final ProviderId provider; private final SemanticCapability capability; private final DimensionRef dimension; private final ControlLease lease; private final ControlAuthority authority;
    protected ControlRequest(ControlDomain domain,ProviderId provider,SemanticCapability capability,DimensionRef dimension,ControlLease lease,ControlAuthority authority){if(domain==null||provider==null||capability==null||dimension==null||lease==null||authority==null)throw new IllegalArgumentException("invalid control request");this.domain=domain;this.provider=provider;this.capability=capability;this.dimension=dimension;this.lease=lease;this.authority=authority;}
    public ControlDomain getDomain(){return domain;} public ProviderId getProvider(){return provider;} public SemanticCapability getCapability(){return capability;} public DimensionRef getDimension(){return dimension;} public ControlLease getLease(){return lease;} public ControlAuthority getAuthority(){return authority;}
}
