package com.sobrenaturaldirector.threat;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import com.sobrenaturaldirector.content.model.ProviderId;

/** Durable, provider-neutral lifecycle record for one controlled threat request. */
public final class ThreatJournalEntry {
    private final String requestId, threatInstanceId, providerId, definitionId, entityUuid, origin;
    private final ThreatLifecycleState state;
    private final int dimension, x, y, z;
    private final long creationTick, resolutionTick;

    public ThreatJournalEntry(String requestId, String threatInstanceId, ProviderId providerId, String definitionId,
            UUID entityUuid, String origin, ThreatLifecycleState state, int dimension, int x, int y, int z,
            long creationTick, long resolutionTick) {
        this.requestId = required(requestId); this.threatInstanceId = required(threatInstanceId);
        if (providerId == null) throw new IllegalArgumentException("providerId is required");
        this.providerId = providerId.getValue(); this.definitionId = required(definitionId);
        this.entityUuid = entityUuid == null ? "" : entityUuid.toString(); this.origin = required(origin);
        if (state == null) throw new IllegalArgumentException("state is required");
        this.state = state; this.dimension = dimension; this.x = x; this.y = y; this.z = z;
        this.creationTick = creationTick; this.resolutionTick = resolutionTick;
    }
    private static String required(String value) { if (value == null || value.length() == 0 || value.length() > 128) throw new IllegalArgumentException("invalid threat journal text"); return value; }
    public String getRequestId() { return requestId; }
    public String getThreatInstanceId() { return threatInstanceId; }
    public ProviderId getProviderId() { return new ProviderId(providerId); }
    public String getDefinitionId() { return definitionId; }
    public UUID getEntityUuid() { return entityUuid.length() == 0 ? null : UUID.fromString(entityUuid); }
    public String getOrigin() { return origin; }
    public ThreatLifecycleState getState() { return state; }
    public int getDimension() { return dimension; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public long getCreationTick() { return creationTick; }
    public long getResolutionTick() { return resolutionTick; }
    public ThreatJournalEntry withState(ThreatLifecycleState next, UUID nextUuid, long tick) {
        return new ThreatJournalEntry(requestId, threatInstanceId, getProviderId(), definitionId, nextUuid == null ? getEntityUuid() : nextUuid,
                origin, next, dimension, x, y, z, creationTick, tick);
    }
    public NBTTagCompound toNbt() {
        NBTTagCompound n = new NBTTagCompound(); n.setInteger("schemaVersion", ThreatOwnership.SCHEMA_VERSION);
        n.setString("requestId", requestId); n.setString("threatInstanceId", threatInstanceId); n.setString("providerId", providerId);
        n.setString("definitionId", definitionId); n.setString("entityUuid", entityUuid); n.setString("origin", origin);
        n.setString("state", state.name()); n.setInteger("dimension", dimension); n.setInteger("x", x); n.setInteger("y", y); n.setInteger("z", z);
        n.setLong("creationTick", creationTick); n.setLong("resolutionTick", resolutionTick); return n;
    }
    public static ThreatJournalEntry fromNbt(NBTTagCompound n) {
        if (n == null || n.getInteger("schemaVersion") != ThreatOwnership.SCHEMA_VERSION) throw new IllegalArgumentException("invalid threat journal schema");
        return new ThreatJournalEntry(n.getString("requestId"), n.getString("threatInstanceId"), new ProviderId(n.getString("providerId")),
                n.getString("definitionId"), n.getString("entityUuid").length() == 0 ? null : UUID.fromString(n.getString("entityUuid")),
                n.getString("origin"), ThreatLifecycleState.valueOf(n.getString("state")), n.getInteger("dimension"), n.getInteger("x"),
                n.getInteger("y"), n.getInteger("z"), n.getLong("creationTick"), n.getLong("resolutionTick"));
    }
}
