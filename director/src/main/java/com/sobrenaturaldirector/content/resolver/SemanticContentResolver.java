package com.sobrenaturaldirector.content.resolver;

import java.util.*;
import com.sobrenaturaldirector.content.catalog.SemanticCatalog;
import com.sobrenaturaldirector.content.model.*;
import com.sobrenaturaldirector.content.query.ResolutionRequest;
import com.sobrenaturaldirector.content.query.ResolutionResult;
import com.sobrenaturaldirector.decision.model.ProviderStatus;

public final class SemanticContentResolver {
    public ResolutionResult resolve(ResolutionRequest request, SemanticCatalog catalog, RuntimeContentIndex runtime) {
        if(request==null||catalog==null)return new ResolutionResult(ResolutionResult.Status.UNKNOWN,Collections.<ResolutionResult.Candidate>emptyList(),"CATALOG_OR_REQUEST_UNKNOWN",0);
        List<ResolutionResult.Candidate> matches=new ArrayList<ResolutionResult.Candidate>();int examined=0;boolean evidenceMiss=false,providerMiss=false,contentMiss=false,adapter=false;
        for(SemanticContentEntry e:catalog.getEntries().values()) { examined++; if(!request.getKinds().isEmpty()&&!request.getKinds().contains(e.getKind()))continue;if(request.getExcluded().contains(e.getProvider()))continue;if(!e.getCapabilities().containsAll(request.getRequired()))continue;if(e.getEvidence().ordinal()<request.getRequiredEvidence().ordinal()){evidenceMiss=true;continue;}
            AvailabilityStatus a;ExecutionStatus x=e.getExecution();String reason="";ProviderStatus ps=runtime==null?null:runtime.providerStatus(e.getProvider());
            if(e.isAdapterRequired()||!e.isRegistryResolvable()){if(request.getAdapterPolicy()==ResolutionRequest.AdapterPolicy.REJECT)continue;a=AvailabilityStatus.ADAPTER_REQUIRED;adapter=true;reason="ADAPTER_REQUIRED";}
            else if(runtime==null){a=AvailabilityStatus.UNKNOWN;reason="RUNTIME_INDEX_UNKNOWN";}
            else if(ps==ProviderStatus.MISSING||ps==ProviderStatus.DISABLED){a=AvailabilityStatus.PROVIDER_MISSING;providerMiss=true;reason="PROVIDER_MISSING";}
            else if(ps!=ProviderStatus.AVAILABLE){a=ps==ProviderStatus.FAILED?AvailabilityStatus.RUNTIME_BLOCKED:AvailabilityStatus.UNKNOWN;reason="PROVIDER_STATUS_UNKNOWN";}
            else if(!runtime.contains(e.getKind(),e.getKey())){a=AvailabilityStatus.REGISTRY_ENTRY_MISSING;contentMiss=true;reason="REGISTRY_ENTRY_MISSING";}
            else {a=AvailabilityStatus.AVAILABLE;x=ExecutionStatus.ELIGIBLE_FOR_FUTURE_EVALUATION;reason="RUNTIME_KEY_PRESENT";}
            matches.add(new ResolutionResult.Candidate(e,a,x,reason));
        }
        Collections.sort(matches,new Comparator<ResolutionResult.Candidate>(){public int compare(ResolutionResult.Candidate a,ResolutionResult.Candidate b){int pa=preference(a.getEntry().getProvider(),request.getPreferred()),pb=preference(b.getEntry().getProvider(),request.getPreferred());if(pa!=pb)return pa-pb;return a.getEntry().getKey().compareTo(b.getEntry().getKey());}});
        List<ResolutionResult.Candidate> available=new ArrayList<ResolutionResult.Candidate>();for(ResolutionResult.Candidate c:matches)if(c.getAvailability()==AvailabilityStatus.AVAILABLE)available.add(c);
        if(!available.isEmpty())return new ResolutionResult(available.size()==1?ResolutionResult.Status.RESOLVED:ResolutionResult.Status.MULTIPLE_CANDIDATES,available,"CAPABILITIES_RESOLVED",examined);
        if(adapter)return new ResolutionResult(ResolutionResult.Status.ADAPTER_REQUIRED,matches,"ADAPTER_REQUIRED",examined);if(providerMiss)return new ResolutionResult(ResolutionResult.Status.PROVIDER_MISSING,matches,"PROVIDER_MISSING",examined);if(contentMiss)return new ResolutionResult(ResolutionResult.Status.CONTENT_MISSING,matches,"REGISTRY_ENTRY_MISSING",examined);if(evidenceMiss)return new ResolutionResult(ResolutionResult.Status.INSUFFICIENT_EVIDENCE,matches,"INSUFFICIENT_EVIDENCE",examined);return new ResolutionResult(runtime==null?ResolutionResult.Status.UNKNOWN:ResolutionResult.Status.UNRESOLVED,matches,"NO_VALID_CANDIDATE",examined);
    }
    private int preference(ProviderId id,List<ProviderId> preferred){int i=preferred.indexOf(id);return i<0?preferred.size():i;}
}
