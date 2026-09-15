package com.sobrenaturaldirector.content.model;

import java.util.*;
import com.sobrenaturaldirector.decision.model.ProviderStatus;

public final class RuntimeContentIndex {
    private final Map<ProviderId,ProviderStatus> providers; private final Set<ContentKey> items,blocks,entities;
    public RuntimeContentIndex(Map<ProviderId,ProviderStatus> providers,Collection<ContentKey> items,Collection<ContentKey> blocks,Collection<ContentKey> entities) {
        this.providers=copy(providers);this.items=keys(items);this.blocks=keys(blocks);this.entities=keys(entities);
    }
    private static <K,V> Map<K,V> copy(Map<K,V> m){return Collections.unmodifiableMap(new TreeMap<K,V>(m==null?Collections.<K,V>emptyMap():m));}
    private static Set<ContentKey> keys(Collection<ContentKey> c){TreeSet<ContentKey>s=new TreeSet<ContentKey>();if(c!=null)s.addAll(c);return Collections.unmodifiableSet(s);}
    public Map<ProviderId,ProviderStatus> getProviders(){return providers;}public Set<ContentKey> getItems(){return items;}public Set<ContentKey> getBlocks(){return blocks;}public Set<ContentKey> getEntities(){return entities;}
    public ProviderStatus providerStatus(ProviderId id){ProviderStatus s=providers.get(id);return s==null?ProviderStatus.UNKNOWN:s;}
    public boolean contains(ContentKind kind,ContentKey key){return (kind==ContentKind.ITEM?items:kind==ContentKind.BLOCK?blocks:kind==ContentKind.ENTITY?entities:Collections.<ContentKey>emptySet()).contains(key);}
}
