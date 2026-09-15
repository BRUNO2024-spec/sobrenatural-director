package com.sobrenaturaldirector.mutation.runtime;

import net.minecraft.nbt.NBTTagCompound;

/** Durable audit record for the single allowlisted operation. */
public final class MutationJournalEntry {
    public static final int SCHEMA_VERSION = 1;
    private final String mutationId, planId, sourceId, operation, beforeBlock, afterBlock, status, rollbackStatus;
    private final int dimension, x, y, z, beforeMetadata, afterMetadata;
    private final long tick;
    private final NBTTagCompound beforeTile;

    public MutationJournalEntry(String mutationId, String planId, String sourceId, String operation,
            int dimension, int x, int y, int z, String beforeBlock, int beforeMetadata,
            String afterBlock, int afterMetadata, String status, long tick, NBTTagCompound beforeTile,
            String rollbackStatus) {
        this.mutationId = required(mutationId); this.planId = required(planId); this.sourceId = required(sourceId);
        this.operation = required(operation); this.beforeBlock = required(beforeBlock); this.afterBlock = required(afterBlock);
        this.status = required(status); this.rollbackStatus = required(rollbackStatus);
        this.dimension = dimension; this.x = x; this.y = y; this.z = z;
        this.beforeMetadata = beforeMetadata; this.afterMetadata = afterMetadata; this.tick = tick;
        this.beforeTile = beforeTile == null ? null : (NBTTagCompound) beforeTile.copy();
    }
    private static String required(String value) { if (value == null || value.length() == 0 || value.length() > 128) throw new IllegalArgumentException("invalid journal text"); return value; }
    public String getMutationId() { return mutationId; }
    public String getPlanId() { return planId; }
    public String getSourceId() { return sourceId; }
    public String getOperation() { return operation; }
    public int getDimension() { return dimension; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public String getBeforeBlock() { return beforeBlock; }
    public int getBeforeMetadata() { return beforeMetadata; }
    public String getAfterBlock() { return afterBlock; }
    public int getAfterMetadata() { return afterMetadata; }
    public String getStatus() { return status; }
    public String getRollbackStatus() { return rollbackStatus; }
    public long getTick() { return tick; }
    public NBTTagCompound getBeforeTile() { return beforeTile == null ? null : (NBTTagCompound) beforeTile.copy(); }
    public MutationJournalEntry withStatus(String nextStatus, String nextRollbackStatus, long nextTick) {
        return new MutationJournalEntry(mutationId, planId, sourceId, operation, dimension, x, y, z,
                beforeBlock, beforeMetadata, afterBlock, afterMetadata, nextStatus, nextTick, beforeTile, nextRollbackStatus);
    }
    public NBTTagCompound toNbt() {
        NBTTagCompound n = new NBTTagCompound(); n.setInteger("schemaVersion", SCHEMA_VERSION);
        n.setString("mutationId", mutationId); n.setString("planId", planId); n.setString("sourceId", sourceId);
        n.setString("operation", operation); n.setInteger("dimension", dimension); n.setInteger("x", x); n.setInteger("y", y); n.setInteger("z", z);
        n.setString("beforeBlock", beforeBlock); n.setInteger("beforeMetadata", beforeMetadata);
        n.setString("afterBlock", afterBlock); n.setInteger("afterMetadata", afterMetadata);
        n.setString("status", status); n.setString("rollbackStatus", rollbackStatus); n.setLong("tick", tick);
        if (beforeTile != null) n.setTag("beforeTile", beforeTile.copy());
        return n;
    }
    public static MutationJournalEntry fromNbt(NBTTagCompound n) {
        if (n == null || n.getInteger("schemaVersion") != SCHEMA_VERSION) throw new IllegalArgumentException("invalid mutation journal schema");
        return new MutationJournalEntry(n.getString("mutationId"), n.getString("planId"), n.getString("sourceId"), n.getString("operation"),
                n.getInteger("dimension"), n.getInteger("x"), n.getInteger("y"), n.getInteger("z"), n.getString("beforeBlock"), n.getInteger("beforeMetadata"),
                n.getString("afterBlock"), n.getInteger("afterMetadata"), n.getString("status"), n.getLong("tick"),
                n.hasKey("beforeTile", 10) ? n.getCompoundTag("beforeTile") : null, n.getString("rollbackStatus"));
    }
}
