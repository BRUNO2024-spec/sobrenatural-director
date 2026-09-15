package com.sobrenaturaldirector.environment.minecraft;

import java.util.EnumMap;
import java.util.Map;
import com.sobrenaturaldirector.environment.model.EnvironmentEvidence;
import com.sobrenaturaldirector.environment.model.EnvironmentSignal;
import com.sobrenaturaldirector.environment.model.RegionKey;
import net.minecraft.block.Block;
import net.minecraft.world.WorldServer;

/** Loaded-chunk-only scanner. It samples a fixed grid instead of reconstructing terrain. */
public final class MinecraftEnvironmentScanner {
    public static final int MAX_CHUNKS=16, MAX_SAMPLES=512;
    private static long scanCount;
    public static long getScanCount(){return scanCount;}
    public EnvironmentEvidence scan(WorldServer world,int chunkX,int chunkZ,long tick,int players,long lastActivityTick,boolean directorOwned){
        scanCount++;
        RegionKey region=RegionKey.fromChunk(world.provider.dimensionId,chunkX,chunkZ);Map<EnvironmentSignal,Integer> counts=new EnumMap<EnvironmentSignal,Integer>(EnvironmentSignal.class);int loaded=0,samples=0;
        int minChunkX=region.getX()*RegionKey.CHUNKS_PER_REGION,minChunkZ=region.getZ()*RegionKey.CHUNKS_PER_REGION;
        for(int cz=minChunkZ;cz<minChunkZ+RegionKey.CHUNKS_PER_REGION&&loaded<MAX_CHUNKS;cz++)for(int cx=minChunkX;cx<minChunkX+RegionKey.CHUNKS_PER_REGION&&loaded<MAX_CHUNKS;cx++){
            if(!world.getChunkProvider().chunkExists(cx,cz))continue;loaded++;int minX=cx<<4,minZ=cz<<4;
            for(int z=minZ;z<minZ+16&&samples<MAX_SAMPLES;z+=4)for(int x=minX;x<minX+16&&samples<MAX_SAMPLES;x+=4){
                int y=world.getTopSolidOrLiquidBlock(x,z);if(y<1)y=64;
                for(int depth=0;depth<6&&samples<MAX_SAMPLES;depth++){Block block=world.getBlock(x,y+2-depth,z);String name=String.valueOf(Block.blockRegistry.getNameForObject(block)).toLowerCase();classify(name,counts);samples++;}
            }
        }
        return new EnvironmentEvidence(region,tick,counts,samples,loaded,players,lastActivityTick,isVillage(world,region),directorOwned);
    }
    private static boolean isVillage(WorldServer world,RegionKey region){
        try { for(Class<?> type=world.getClass();type!=null;type=type.getSuperclass())for(java.lang.reflect.Field field:type.getDeclaredFields()){field.setAccessible(true);Object value=field.get(world);if(value==null||!field.getType().getName().contains("VillageCollection"))continue;Object list=value.getClass().getMethod("getVillageList").invoke(value);if(list instanceof Iterable)for(Object village:(Iterable<?>)list){Object center=village.getClass().getMethod("getCenter").invoke(village);int radius=((Number)village.getClass().getMethod("getVillageRadius").invoke(village)).intValue();java.lang.reflect.Field px=center.getClass().getField("posX"),pz=center.getClass().getField("posZ");int x=((Number)px.get(center)).intValue();int z=((Number)pz.get(center)).intValue();int min=region.getX()*RegionKey.CHUNKS_PER_REGION*16,max=min+RegionKey.CHUNKS_PER_REGION*16;if(x+radius>=min&&x-radius<=max&&z+radius>=min&&z-radius<=max)return true;}}}catch(Throwable ignored){} return false;
    }
    private static void classify(String n,Map<EnvironmentSignal,Integer> c){
        if(n.contains("torch")||n.contains("lantern")||n.contains("glowstone")||n.contains("lamp"))add(c,EnvironmentSignal.LIGHT_SOURCE);
        if(n.contains("chest")||n.contains("barrel")||n.contains("storage")||n.contains("crate"))add(c,EnvironmentSignal.STORAGE);
        if(n.contains("crafting")||n.contains("workbench")||n.contains("anvil"))add(c,EnvironmentSignal.CRAFTING);
        if(n.contains("furnace")||n.contains("smelter"))add(c,EnvironmentSignal.SMELTING);
        if(n.contains("bed"))add(c,EnvironmentSignal.SLEEPING);
        if(n.contains("fence")||n.contains("wall")||n.contains("iron_door")||n.contains("gate"))add(c,EnvironmentSignal.FORTIFICATION);
        if(n.contains("door")||n.contains("trapdoor")||n.contains("button")||n.contains("lever"))add(c,EnvironmentSignal.ACCESS_CONTROL);
        if(n.contains("redstone")||n.contains("piston"))add(c,EnvironmentSignal.REDSTONE);
        if(n.contains("farmland")||n.contains("crop")||n.contains("wheat"))add(c,EnvironmentSignal.FARMING);
        if(n.contains("plank")||n.contains("brick")||n.contains("glass")||n.contains("stonebrick"))add(c,EnvironmentSignal.PLAYER_MODIFICATION);
    }
    private static void add(Map<EnvironmentSignal,Integer> c,EnvironmentSignal s){Integer n=c.get(s);c.put(s,n==null?1:n+1);}
}
