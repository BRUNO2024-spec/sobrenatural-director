package com.sobrenaturaldirector.composition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Durable group record; child state is registry identity plus coordinates only. */
public final class CompositionJournalEntry {
    public static final int SCHEMA_VERSION = 1;
    public static final class Child {
        public final String id, block;
        public final int x, y, z, metadata;
        public Child(String id, String block, int metadata, int x, int y, int z) { this.id=id; this.block=block; this.metadata=metadata; this.x=x; this.y=y; this.z=z; }
    }
    private final String id, source, provider, version, status;
    private final int dimension, anchorX, anchorY, anchorZ, minX, minY, minZ, maxX, maxY, maxZ;
    private final long tick;
    private final List<Child> children;
    public CompositionJournalEntry(String id, String source, String provider, String version, String status,
            int dimension, int anchorX, int anchorY, int anchorZ, int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ, long tick, List<Child> children) {
        this.id=id; this.source=source; this.provider=provider; this.version=version; this.status=status;
        this.dimension=dimension; this.anchorX=anchorX; this.anchorY=anchorY; this.anchorZ=anchorZ;
        this.minX=minX; this.minY=minY; this.minZ=minZ; this.maxX=maxX; this.maxY=maxY; this.maxZ=maxZ; this.tick=tick;
        this.children=Collections.unmodifiableList(new ArrayList<Child>(children));
    }
    public String getId(){return id;} public String getSource(){return source;} public String getProvider(){return provider;} public String getVersion(){return version;} public String getStatus(){return status;}
    public int getDimension(){return dimension;} public int getAnchorX(){return anchorX;} public int getAnchorY(){return anchorY;} public int getAnchorZ(){return anchorZ;}
    public int getMinX(){return minX;} public int getMinY(){return minY;} public int getMinZ(){return minZ;} public int getMaxX(){return maxX;} public int getMaxY(){return maxY;} public int getMaxZ(){return maxZ;}
    public long getTick(){return tick;} public List<Child> getChildren(){return children;}
    public CompositionJournalEntry withStatus(String next, long nextTick){return new CompositionJournalEntry(id,source,provider,version,next,dimension,anchorX,anchorY,anchorZ,minX,minY,minZ,maxX,maxY,maxZ,nextTick,children);}
    public NBTTagCompound toNbt(){NBTTagCompound n=new NBTTagCompound();n.setInteger("schemaVersion",SCHEMA_VERSION);n.setString("id",id);n.setString("source",source);n.setString("provider",provider);n.setString("version",version);n.setString("status",status);n.setInteger("dimension",dimension);n.setInteger("anchorX",anchorX);n.setInteger("anchorY",anchorY);n.setInteger("anchorZ",anchorZ);n.setInteger("minX",minX);n.setInteger("minY",minY);n.setInteger("minZ",minZ);n.setInteger("maxX",maxX);n.setInteger("maxY",maxY);n.setInteger("maxZ",maxZ);n.setLong("tick",tick);NBTTagList l=new NBTTagList();for(Child c:children){NBTTagCompound x=new NBTTagCompound();x.setString("id",c.id);x.setString("block",c.block);x.setInteger("metadata",c.metadata);x.setInteger("x",c.x);x.setInteger("y",c.y);x.setInteger("z",c.z);l.appendTag(x);}n.setTag("children",l);return n;}
    public static CompositionJournalEntry fromNbt(NBTTagCompound n){if(n==null||n.getInteger("schemaVersion")!=SCHEMA_VERSION)throw new IllegalArgumentException("invalid composition journal schema");List<Child> c=new ArrayList<Child>();NBTTagList l=n.getTagList("children",10);for(int i=0;i<l.tagCount();i++){NBTTagCompound x=l.getCompoundTagAt(i);c.add(new Child(x.getString("id"),x.getString("block"),x.getInteger("metadata"),x.getInteger("x"),x.getInteger("y"),x.getInteger("z")));}return new CompositionJournalEntry(n.getString("id"),n.getString("source"),n.getString("provider"),n.getString("version"),n.getString("status"),n.getInteger("dimension"),n.getInteger("anchorX"),n.getInteger("anchorY"),n.getInteger("anchorZ"),n.getInteger("minX"),n.getInteger("minY"),n.getInteger("minZ"),n.getInteger("maxX"),n.getInteger("maxY"),n.getInteger("maxZ"),n.getLong("tick"),c);}
}
