package com.sobrenaturaldirector.naturalpressure;

import com.sobrenaturaldirector.domain.StableId;
import net.minecraft.nbt.NBTTagCompound;

public final class NaturalThreatPressureDirective {
    private final String directiveId, provider, category, reason;
    private final NaturalThreatPressureScope scope;
    private final NaturalThreatPressureLevel level;
    private final int priority;
    private final long createdAt, expiresAt;
    public NaturalThreatPressureDirective(String directiveId, String provider, String category, NaturalThreatPressureScope scope, NaturalThreatPressureLevel level, int priority, long createdAt, long expiresAt, String reason) {
        this.directiveId=StableId.require(directiveId,"directiveId"); this.provider=StableId.require(provider,"provider"); this.category=category == null || category.length()==0 ? "" : StableId.require(category,"category"); if(scope==null||level==null||createdAt<0||expiresAt<=createdAt) throw new IllegalArgumentException("invalid pressure directive"); this.scope=scope; this.level=level; this.priority=priority; this.createdAt=createdAt; this.expiresAt=expiresAt; this.reason=StableId.require(reason,"reason");
    }
    public String getDirectiveId(){return directiveId;} public String getProvider(){return provider;} public String getCategory(){return category;} public NaturalThreatPressureScope getScope(){return scope;} public NaturalThreatPressureLevel getLevel(){return level;} public int getPriority(){return priority;} public long getCreatedAt(){return createdAt;} public long getExpiresAt(){return expiresAt;} public String getReason(){return reason;}
    public boolean isExpired(long tick){return tick>=expiresAt;}
    public NBTTagCompound toNbt(){NBTTagCompound n=new NBTTagCompound();n.setString("id",directiveId);n.setString("provider",provider);n.setString("category",category);n.setInteger("dimension",scope.getDimension());n.setInteger("minX",scope.getMinChunkX());n.setInteger("maxX",scope.getMaxChunkX());n.setInteger("minZ",scope.getMinChunkZ());n.setInteger("maxZ",scope.getMaxChunkZ());n.setString("level",level.name());n.setInteger("priority",priority);n.setLong("createdAt",createdAt);n.setLong("expiresAt",expiresAt);n.setString("reason",reason);return n;}
    public static NaturalThreatPressureDirective fromNbt(NBTTagCompound n){int minX=n.getInteger("minX"),maxX=n.getInteger("maxX"),minZ=n.getInteger("minZ"),maxZ=n.getInteger("maxZ");NaturalThreatPressureScope s=minX==Integer.MIN_VALUE&&maxX==Integer.MAX_VALUE&&minZ==Integer.MIN_VALUE&&maxZ==Integer.MAX_VALUE?NaturalThreatPressureScope.world(n.getInteger("dimension")):NaturalThreatPressureScope.region(n.getInteger("dimension"),minX,maxX,minZ,maxZ);return new NaturalThreatPressureDirective(n.getString("id"),n.getString("provider"),n.getString("category"),s,NaturalThreatPressureLevel.valueOf(n.getString("level")),n.getInteger("priority"),n.getLong("createdAt"),n.getLong("expiresAt"),n.getString("reason"));}
}
