package com.sobrenaturaldirector.control;

/** A spatial reference that cannot be constructed without a dimension. */
public final class WorldPositionRef {
    private final DimensionRef dimension;
    private final int x, y, z;
    public WorldPositionRef(DimensionRef dimension, int x, int y, int z) {
        if (dimension == null) throw new IllegalArgumentException("dimension is required");
        this.dimension = dimension; this.x = x; this.y = y; this.z = z;
    }
    public DimensionRef getDimension() { return dimension; }
    public int getX() { return x; } public int getY() { return y; } public int getZ() { return z; }
    @Override public boolean equals(Object o) { if (!(o instanceof WorldPositionRef)) return false; WorldPositionRef p=(WorldPositionRef)o; return dimension.equals(p.dimension)&&x==p.x&&y==p.y&&z==p.z; }
    @Override public int hashCode() { int h=dimension.hashCode(); h=31*h+x; h=31*h+y; return 31*h+z; }
}
