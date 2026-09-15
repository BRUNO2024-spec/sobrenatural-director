package com.sobrenaturaldirector.threat;

import java.util.UUID;
import com.sobrenaturaldirector.content.model.ProviderId;
import net.minecraft.nbt.NBTTagCompound;

/** Durable binding metadata for one replaceable provider-owned physical instance. */
public final class ThreatPhysicalBinding {
    public static final int SCHEMA_VERSION = 1;
    private final String threatId, providerId, entityUuid, status;
    private final int dimension, x, y, z, generation;
    private final long lastSeenTick;
    public ThreatPhysicalBinding(String threatId, ProviderId providerId, UUID entityUuid, String status, int dimension, int x, int y, int z, int generation, long lastSeenTick) {
        if (threatId == null || threatId.length() == 0 || providerId == null || entityUuid == null || status == null) throw new IllegalArgumentException("invalid threat binding");
        this.threatId = threatId; this.providerId = providerId.getValue(); this.entityUuid = entityUuid.toString(); this.status = status; this.dimension = dimension; this.x = x; this.y = y; this.z = z; this.generation = generation; this.lastSeenTick = lastSeenTick;
    }
    public String getThreatId() { return threatId; } public ProviderId getProviderId() { return new ProviderId(providerId); } public UUID getEntityUuid() { return UUID.fromString(entityUuid); } public String getStatus() { return status; }
    public int getDimension() { return dimension; } public int getX() { return x; } public int getY() { return y; } public int getZ() { return z; } public int getGeneration() { return generation; } public long getLastSeenTick() { return lastSeenTick; }
    public ThreatPhysicalBinding withStatus(String next, int nextGeneration, long tick) { return new ThreatPhysicalBinding(threatId, getProviderId(), getEntityUuid(), next, dimension, x, y, z, nextGeneration, tick); }
    public NBTTagCompound toNbt() { NBTTagCompound n = new NBTTagCompound(); n.setInteger("schemaVersion", SCHEMA_VERSION); n.setString("threatId", threatId); n.setString("providerId", providerId); n.setString("entityUuid", entityUuid); n.setString("status", status); n.setInteger("dimension", dimension); n.setInteger("x", x); n.setInteger("y", y); n.setInteger("z", z); n.setInteger("generation", generation); n.setLong("lastSeenTick", lastSeenTick); return n; }
    public static ThreatPhysicalBinding fromNbt(NBTTagCompound n) { if (n == null || n.getInteger("schemaVersion") != SCHEMA_VERSION) throw new IllegalArgumentException("invalid threat binding schema"); return new ThreatPhysicalBinding(n.getString("threatId"), new ProviderId(n.getString("providerId")), UUID.fromString(n.getString("entityUuid")), n.getString("status"), n.getInteger("dimension"), n.getInteger("x"), n.getInteger("y"), n.getInteger("z"), n.getInteger("generation"), n.getLong("lastSeenTick")); }
}
