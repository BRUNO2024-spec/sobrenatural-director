package com.sobrenaturaldirector.composition.model;

public final class LogicalPosition {
    private final int x,y,z;
    public LogicalPosition(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
    public int getX(){return x;}public int getY(){return y;}public int getZ(){return z;}
    @Override public boolean equals(Object o){if(!(o instanceof LogicalPosition))return false;LogicalPosition p=(LogicalPosition)o;return x==p.x&&y==p.y&&z==p.z;}
    @Override public int hashCode(){return (x*31+y)*31+z;}
}
