package com.sobrenaturaldirector.threat;

import com.sobrenaturaldirector.content.model.ProviderId;
import net.minecraft.nbt.NBTTagCompound;

/** Durable narrative identity. Physical entity UUIDs are intentionally not stored here. */
public final class PersistentNarrativeThreat {
    public static final int SCHEMA_VERSION = 1;
    private final String id, requestId, providerId, semanticType, origin;
    private final ThreatLifecycleState state;
    private final int dimension, x, y, z;
    private final long createdTick, updatedTick, resolvedTick;
    private final boolean outcomeConfirmed;

    public PersistentNarrativeThreat(String id, String requestId, ProviderId providerId, String semanticType,
            String origin, ThreatLifecycleState state, int dimension, int x, int y, int z,
            long createdTick, long updatedTick, long resolvedTick) {
        this(id, requestId, providerId, semanticType, origin, state, dimension, x, y, z, createdTick, updatedTick, resolvedTick, false);
    }
    /** Runtime fixtures may omit a resolved tick; synthesis never calls this constructor. */
    public PersistentNarrativeThreat(String id, String requestId, ProviderId providerId, String semanticType,
            String origin, ThreatLifecycleState state, int dimension, int x, int y, int z,
            long createdTick, long updatedTick) {
        this(id, requestId, providerId, semanticType, origin, state, dimension, x, y, z, createdTick, updatedTick, 0L, false);
    }
    public PersistentNarrativeThreat(String id, String requestId, ProviderId providerId, String semanticType,
            String origin, ThreatLifecycleState state, int dimension, int x, int y, int z,
            long createdTick, long updatedTick, long resolvedTick, boolean outcomeConfirmed) {
        this.id = required(id); this.requestId = required(requestId); if (providerId == null) throw new IllegalArgumentException("providerId is required");
        this.providerId = providerId.getValue(); this.semanticType = required(semanticType); this.origin = required(origin);
        if (state == null) throw new IllegalArgumentException("state is required"); this.state = state;
        this.dimension = dimension; this.x = x; this.y = y; this.z = z; this.createdTick = createdTick; this.updatedTick = updatedTick; this.resolvedTick = resolvedTick; this.outcomeConfirmed = outcomeConfirmed;
    }
    private static String required(String value) { if (value == null || value.length() == 0 || value.length() > 128) throw new IllegalArgumentException("invalid narrative threat text"); return value; }
    public String getId() { return id; } public String getRequestId() { return requestId; } public ProviderId getProviderId() { return new ProviderId(providerId); }
    public String getSemanticType() { return semanticType; } public String getOrigin() { return origin; } public ThreatLifecycleState getState() { return state; }
    public int getDimension() { return dimension; } public int getX() { return x; } public int getY() { return y; } public int getZ() { return z; }
    public long getCreatedTick() { return createdTick; } public long getUpdatedTick() { return updatedTick; } public long getResolvedTick() { return resolvedTick; }
    public boolean isOutcomeConfirmed() { return outcomeConfirmed; }
    public PersistentNarrativeThreat withState(ThreatLifecycleState next, long tick) { return new PersistentNarrativeThreat(id, requestId, getProviderId(), semanticType, origin, next, dimension, x, y, z, createdTick, tick, next == ThreatLifecycleState.RESOLVED ? tick : resolvedTick, outcomeConfirmed); }
    public PersistentNarrativeThreat withOutcomeConfirmed(boolean confirmed, long tick) { return new PersistentNarrativeThreat(id, requestId, getProviderId(), semanticType, origin, state, dimension, x, y, z, createdTick, tick, resolvedTick, confirmed); }
    public NBTTagCompound toNbt() { NBTTagCompound n = new NBTTagCompound(); n.setInteger("schemaVersion", SCHEMA_VERSION); n.setString("id", id); n.setString("requestId", requestId); n.setString("providerId", providerId); n.setString("semanticType", semanticType); n.setString("origin", origin); n.setString("state", state.name()); n.setInteger("dimension", dimension); n.setInteger("x", x); n.setInteger("y", y); n.setInteger("z", z); n.setLong("createdTick", createdTick); n.setLong("updatedTick", updatedTick); n.setLong("resolvedTick", resolvedTick); n.setBoolean("outcomeConfirmed", outcomeConfirmed); return n; }
    public static PersistentNarrativeThreat fromNbt(NBTTagCompound n) { if (n == null || n.getInteger("schemaVersion") != SCHEMA_VERSION) throw new IllegalArgumentException("invalid narrative threat schema"); return new PersistentNarrativeThreat(n.getString("id"), n.getString("requestId"), new ProviderId(n.getString("providerId")), n.getString("semanticType"), n.getString("origin"), ThreatLifecycleState.valueOf(n.getString("state")), n.getInteger("dimension"), n.getInteger("x"), n.getInteger("y"), n.getInteger("z"), n.getLong("createdTick"), n.getLong("updatedTick"), n.getLong("resolvedTick"), n.getBoolean("outcomeConfirmed")); }
}
