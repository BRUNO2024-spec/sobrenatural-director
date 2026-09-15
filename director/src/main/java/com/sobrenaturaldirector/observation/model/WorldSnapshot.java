package com.sobrenaturaldirector.observation.model;

public final class WorldSnapshot implements Comparable<WorldSnapshot> {
    private final int dimensionId;
    private final long totalWorldTime;
    private final long worldTime;
    private final int difficultyId;
    private final boolean raining;
    private final boolean thundering;
    private final int playerCount;

    public WorldSnapshot(int dimensionId, long totalWorldTime, long worldTime, int difficultyId, boolean raining, boolean thundering, int playerCount) {
        if (totalWorldTime < 0 || playerCount < 0) throw new IllegalArgumentException("invalid world snapshot");
        this.dimensionId=dimensionId; this.totalWorldTime=totalWorldTime; this.worldTime=worldTime; this.difficultyId=difficultyId; this.raining=raining; this.thundering=thundering; this.playerCount=playerCount;
    }
    public int getDimensionId(){return dimensionId;} public long getTotalWorldTime(){return totalWorldTime;} public long getWorldTime(){return worldTime;} public int getDifficultyId(){return difficultyId;} public boolean isRaining(){return raining;} public boolean isThundering(){return thundering;} public int getPlayerCount(){return playerCount;}
    @Override public int compareTo(WorldSnapshot other){return dimensionId<other.dimensionId?-1:(dimensionId==other.dimensionId?0:1);}
    @Override public boolean equals(Object o){if(!(o instanceof WorldSnapshot))return false;WorldSnapshot x=(WorldSnapshot)o;return dimensionId==x.dimensionId&&totalWorldTime==x.totalWorldTime&&worldTime==x.worldTime&&difficultyId==x.difficultyId&&raining==x.raining&&thundering==x.thundering&&playerCount==x.playerCount;}@Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{dimensionId,totalWorldTime,worldTime,difficultyId,raining,thundering,playerCount});}
}
