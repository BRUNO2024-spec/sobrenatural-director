package com.sobrenaturaldirector.spatial.geometry;

import java.util.*;

public final class LocalVec3i implements Comparable<LocalVec3i> { private final int x,y,z;public LocalVec3i(int x,int y,int z){this.x=x;this.y=y;this.z=z;}public int getX(){return x;}public int getY(){return y;}public int getZ(){return z;}public LocalVec3i add(int dx,int dy,int dz){return new LocalVec3i(Math.addExact(x,dx),Math.addExact(y,dy),Math.addExact(z,dz));}public LocalVec3i add(LocalVec3i v){return add(v.x,v.y,v.z);}public int compareTo(LocalVec3i o){int c=Integer.compare(x,o.x);if(c==0)c=Integer.compare(y,o.y);return c==0?Integer.compare(z,o.z):c;}@Override public boolean equals(Object o){if(!(o instanceof LocalVec3i))return false;LocalVec3i v=(LocalVec3i)o;return x==v.x&&y==v.y&&z==v.z;}@Override public int hashCode(){return (x*31+y)*31+z;}@Override public String toString(){return "local("+x+","+y+","+z+")";}}
