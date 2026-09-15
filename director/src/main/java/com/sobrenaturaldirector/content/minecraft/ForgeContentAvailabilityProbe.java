package com.sobrenaturaldirector.content.minecraft;

import java.util.*;
import com.sobrenaturaldirector.content.model.*;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EntityList;

/** Read-only Forge boundary. It is deliberately not wired into lifecycle events. */
public final class ForgeContentAvailabilityProbe {
    public RuntimeContentIndex snapshot(Collection<ProviderId> providers,Collection<ContentKey> itemKeys,Collection<ContentKey> blockKeys,Collection<ContentKey> entityKeys) {
        Map<ProviderId,ProviderStatus> states=new TreeMap<ProviderId,ProviderStatus>();if(providers!=null)for(ProviderId p:providers)states.put(p,Loader.isModLoaded(p.getValue())?ProviderStatus.AVAILABLE:ProviderStatus.MISSING);
        Set<ContentKey> items=lookupItems(itemKeys),blocks=lookupBlocks(blockKeys),entities=lookupEntities(entityKeys);return new RuntimeContentIndex(states,items,blocks,entities);
    }
    private Set<ContentKey> lookupItems(Collection<ContentKey> keys){Set<ContentKey>r=new TreeSet<ContentKey>();if(keys!=null)for(ContentKey k:keys){String[]p=k.getValue().split(":",2);if(GameRegistry.findItem(p[0],p[1])!=null)r.add(k);}return r;}
    private Set<ContentKey> lookupBlocks(Collection<ContentKey> keys){Set<ContentKey>r=new TreeSet<ContentKey>();if(keys!=null)for(ContentKey k:keys){String[]p=k.getValue().split(":",2);if(GameRegistry.findBlock(p[0],p[1])!=null)r.add(k);}return r;}
    private Set<ContentKey> lookupEntities(Collection<ContentKey> keys){Set<ContentKey>r=new TreeSet<ContentKey>();if(keys!=null)for(ContentKey k:keys){if(EntityList.stringToClassMapping.containsKey(k.getValue().substring(k.getValue().indexOf(':')+1)))r.add(k);}return r;}
}
