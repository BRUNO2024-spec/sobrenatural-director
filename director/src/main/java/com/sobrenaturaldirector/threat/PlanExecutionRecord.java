package com.sobrenaturaldirector.threat;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import com.sobrenaturaldirector.content.model.ProviderId;

/** Durable one-shot ledger record for a role-scoped plan execution. */
public final class PlanExecutionRecord {
    private final String executionId, parentPlanId, planFingerprint, capability, providerId, requestId, threatInstanceId;
    private final PlanExecutionStatus status;
    private final UUID entityUuid;
    private final boolean outcomeRecorded;
    private final long createdAt, resolvedAt;

    public PlanExecutionRecord(String executionId, String parentPlanId, String planFingerprint, String capability, ProviderId providerId, String requestId, String threatInstanceId, PlanExecutionStatus status, UUID entityUuid, boolean outcomeRecorded, long createdAt, long resolvedAt) {
        this.executionId = text(executionId); this.parentPlanId = text(parentPlanId); this.planFingerprint = text(planFingerprint); this.capability = text(capability); if (providerId == null) throw new IllegalArgumentException("providerId is required"); this.providerId = providerId.getValue(); this.requestId = text(requestId); this.threatInstanceId = threatInstanceId == null ? "" : threatInstanceId; if (status == null) throw new IllegalArgumentException("status is required"); this.status = status; this.entityUuid = entityUuid; this.outcomeRecorded = outcomeRecorded; this.createdAt = createdAt; this.resolvedAt = resolvedAt;
    }
    private static String text(String value) { if (value == null || value.length() == 0 || value.length() > 256) throw new IllegalArgumentException("invalid execution ledger text"); return value; }
    public String getExecutionId() { return executionId; } public String getParentPlanId() { return parentPlanId; } public String getPlanFingerprint() { return planFingerprint; } public String getCapability() { return capability; } public ProviderId getProviderId() { return new ProviderId(providerId); } public String getRequestId() { return requestId; } public String getThreatInstanceId() { return threatInstanceId; } public PlanExecutionStatus getStatus() { return status; } public UUID getEntityUuid() { return entityUuid; } public boolean isOutcomeRecorded() { return outcomeRecorded; } public long getCreatedAt() { return createdAt; } public long getResolvedAt() { return resolvedAt; }
    public PlanExecutionRecord withStatus(PlanExecutionStatus next, UUID uuid, String instance, boolean recorded, long resolved) { return new PlanExecutionRecord(executionId, parentPlanId, planFingerprint, capability, getProviderId(), requestId, instance == null ? threatInstanceId : instance, next, uuid == null ? entityUuid : uuid, recorded, createdAt, resolved); }
    public NBTTagCompound toNbt() { NBTTagCompound n = new NBTTagCompound(); n.setString("executionId", executionId); n.setString("parentPlanId", parentPlanId); n.setString("planFingerprint", planFingerprint); n.setString("capability", capability); n.setString("providerId", providerId); n.setString("requestId", requestId); n.setString("threatInstanceId", threatInstanceId); n.setString("status", status.name()); n.setString("entityUuid", entityUuid == null ? "" : entityUuid.toString()); n.setBoolean("outcomeRecorded", outcomeRecorded); n.setLong("createdAt", createdAt); n.setLong("resolvedAt", resolvedAt); return n; }
    public static PlanExecutionRecord fromNbt(NBTTagCompound n) { return new PlanExecutionRecord(n.getString("executionId"), n.getString("parentPlanId"), n.getString("planFingerprint"), n.getString("capability"), new ProviderId(n.getString("providerId")), n.getString("requestId"), n.getString("threatInstanceId"), PlanExecutionStatus.valueOf(n.getString("status")), n.getString("entityUuid").length() == 0 ? null : UUID.fromString(n.getString("entityUuid")), n.getBoolean("outcomeRecorded"), n.getLong("createdAt"), n.getLong("resolvedAt")); }
}
