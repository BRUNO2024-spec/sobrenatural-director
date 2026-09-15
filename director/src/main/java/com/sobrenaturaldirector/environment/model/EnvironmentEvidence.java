package com.sobrenaturaldirector.environment.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Compact transient evidence; it deliberately contains no block history. */
public final class EnvironmentEvidence {
    private final RegionKey region;
    private final long tick;
    private final Map<EnvironmentSignal,Integer> counts;
    private final int sampledBlocks, loadedChunks, players;
    private final long lastActivityTick;
    private final boolean village, directorOwned;
    public EnvironmentEvidence(RegionKey region,long tick,Map<EnvironmentSignal,Integer> counts,int sampledBlocks,int loadedChunks,int players,long lastActivityTick,boolean village,boolean directorOwned){
        if(region==null||tick<0||sampledBlocks<0||loadedChunks<0||players<0||lastActivityTick<0)throw new IllegalArgumentException("invalid environment evidence");
        EnumMap<EnvironmentSignal,Integer> copy=new EnumMap<EnvironmentSignal,Integer>(EnvironmentSignal.class);
        if(counts!=null)for(Map.Entry<EnvironmentSignal,Integer> e:counts.entrySet())if(e.getKey()!=null&&e.getValue()!=null&&e.getValue()>=0)copy.put(e.getKey(),e.getValue());
        this.region=region;this.tick=tick;this.counts=Collections.unmodifiableMap(copy);this.sampledBlocks=sampledBlocks;this.loadedChunks=loadedChunks;this.players=players;this.lastActivityTick=lastActivityTick;this.village=village;this.directorOwned=directorOwned;
    }
    public RegionKey getRegion(){return region;} public long getTick(){return tick;} public Map<EnvironmentSignal,Integer> getCounts(){return counts;}
    public int count(EnvironmentSignal s){Integer n=counts.get(s);return n==null?0:n;} public int getSampledBlocks(){return sampledBlocks;} public int getLoadedChunks(){return loadedChunks;} public int getPlayers(){return players;} public long getLastActivityTick(){return lastActivityTick;} public boolean isVillage(){return village;} public boolean isDirectorOwned(){return directorOwned;}
    public Set<EnvironmentSignal> signals(){return Collections.unmodifiableSet(EnumSet.copyOf(counts.isEmpty()?EnumSet.noneOf(EnvironmentSignal.class):counts.keySet()));}
}
