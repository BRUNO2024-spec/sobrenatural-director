package com.sobrenaturaldirector.environment.model;

/** Dimension-safe bounded region key. Coordinates are groups of four chunks. */
public final class RegionKey implements Comparable<RegionKey> {
    public static final int CHUNKS_PER_REGION = 4;
    private final int dimension, x, z;
    public RegionKey(int dimension, int x, int z) { this.dimension=dimension; this.x=x; this.z=z; }
    public static RegionKey fromChunk(int dimension, int chunkX, int chunkZ) { return new RegionKey(dimension, Math.floorDiv(chunkX, CHUNKS_PER_REGION), Math.floorDiv(chunkZ, CHUNKS_PER_REGION)); }
    public int getDimension(){return dimension;} public int getX(){return x;} public int getZ(){return z;}
    @Override public int compareTo(RegionKey o){int c=Integer.compare(dimension,o.dimension);if(c!=0)return c;c=Integer.compare(x,o.x);return c!=0?c:Integer.compare(z,o.z);}
    @Override public boolean equals(Object o){if(!(o instanceof RegionKey))return false;RegionKey r=(RegionKey)o;return dimension==r.dimension&&x==r.x&&z==r.z;}
    @Override public int hashCode(){return java.util.Arrays.hashCode(new Object[]{dimension,x,z});}
    @Override public String toString(){return dimension+":"+x+":"+z;}
}
