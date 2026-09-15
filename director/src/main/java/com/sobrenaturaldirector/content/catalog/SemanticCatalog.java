package com.sobrenaturaldirector.content.catalog;

import java.util.*;
import com.sobrenaturaldirector.content.model.*;

public final class SemanticCatalog {
    public static final int SCHEMA=1, MAX_ENTRIES=4096, MAX_PROVIDERS=256;
    private final Map<ProviderId,ProviderDescriptor> providers; private final Map<ContentKey,SemanticContentEntry> entries;
    private final Map<SemanticCapability,Set<ContentKey>> byCapability;
    public SemanticCatalog(Collection<ProviderDescriptor> ps,Collection<SemanticContentEntry> es){
        TreeMap<ProviderId,ProviderDescriptor> p=new TreeMap<ProviderId,ProviderDescriptor>();if(ps!=null)for(ProviderDescriptor x:ps)if(p.put(x.getId(),x)!=null)throw new IllegalArgumentException("duplicate provider");
        TreeMap<ContentKey,SemanticContentEntry> e=new TreeMap<ContentKey,SemanticContentEntry>();if(es!=null)for(SemanticContentEntry x:es){if(!p.containsKey(x.getProvider()))throw new IllegalArgumentException("unknown provider");if(e.put(x.getKey(),x)!=null)throw new IllegalArgumentException("duplicate content key");}
        if(p.size()>MAX_PROVIDERS||e.size()>MAX_ENTRIES)throw new IllegalArgumentException("catalog bound exceeded");
        TreeMap<SemanticCapability,Set<ContentKey>> i=new TreeMap<SemanticCapability,Set<ContentKey>>();for(SemanticContentEntry x:e.values())for(SemanticCapability c:x.getCapabilities()){Set<ContentKey>s=i.get(c);if(s==null){s=new TreeSet<ContentKey>();i.put(c,s);}s.add(x.getKey());}
        TreeMap<SemanticCapability,Set<ContentKey>> frozen=new TreeMap<SemanticCapability,Set<ContentKey>>();for(Map.Entry<SemanticCapability,Set<ContentKey>> x:i.entrySet())frozen.put(x.getKey(),Collections.unmodifiableSet(x.getValue()));
        providers=Collections.unmodifiableMap(p);entries=Collections.unmodifiableMap(e);byCapability=Collections.unmodifiableMap(frozen);
    }
    public Map<ProviderId,ProviderDescriptor> getProviders(){return providers;}public Map<ContentKey,SemanticContentEntry> getEntries(){return entries;}public Map<SemanticCapability,Set<ContentKey>> getCapabilityIndex(){return byCapability;}
    public SemanticContentEntry get(ContentKey key){return entries.get(key);}
}
