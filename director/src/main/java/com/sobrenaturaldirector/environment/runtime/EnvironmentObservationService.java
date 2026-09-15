package com.sobrenaturaldirector.environment.runtime;

import java.util.HashMap;
import java.util.Map;
import com.sobrenaturaldirector.environment.EnvironmentClassifier;
import com.sobrenaturaldirector.environment.EnvironmentProfileCache;
import com.sobrenaturaldirector.environment.minecraft.MinecraftEnvironmentScanner;
import com.sobrenaturaldirector.environment.model.RegionKey;
import com.sobrenaturaldirector.environment.model.SemanticRegionProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

/** Server-side cadence owner. It observes player-centered regions only. */
public final class EnvironmentObservationService {
    private final MinecraftEnvironmentScanner scanner=new MinecraftEnvironmentScanner();private final EnvironmentClassifier classifier=new EnvironmentClassifier();private final EnvironmentProfileCache cache=new EnvironmentProfileCache();private final Map<RegionKey,Long> activity=new HashMap<RegionKey,Long>();private long lastTick=Long.MIN_VALUE;
    public void observe(MinecraftServer server,long tick){if(server==null||server.worldServers==null||lastTick==tick)return;lastTick=tick;for(WorldServer world:server.worldServers){if(world==null||world.playerEntities==null)continue;Map<RegionKey,Integer> players=new HashMap<RegionKey,Integer>();for(Object value:world.playerEntities)if(value instanceof EntityPlayer){EntityPlayer p=(EntityPlayer)value;RegionKey key=RegionKey.fromChunk(world.provider.dimensionId,(int)Math.floor(p.posX)>>4,(int)Math.floor(p.posZ)>>4);Integer count=players.get(key);players.put(key,count==null?1:count+1);activity.put(key,tick);}for(Map.Entry<RegionKey,Integer> e:players.entrySet()){RegionKey key=e.getKey();refresh(world,key.getX()*RegionKey.CHUNKS_PER_REGION,key.getZ()*RegionKey.CHUNKS_PER_REGION,tick,e.getValue(),activity.get(key),false);}}
    }
    public SemanticRegionProfile refresh(WorldServer world,int chunkX,int chunkZ,long tick,int players,long lastActivityTick,boolean directorOwned){long start=System.nanoTime();SemanticRegionProfile profile=classifier.classify(scanner.scan(world,chunkX,chunkZ,tick,players,lastActivityTick,directorOwned));lastScanNanos=System.nanoTime()-start;cache.put(profile);return profile;}
    private long lastScanNanos;
    public long getLastScanNanos(){return lastScanNanos;}
    public EnvironmentProfileCache getCache(){return cache;} public SemanticRegionProfile get(RegionKey key){return cache.get(key);}
}
