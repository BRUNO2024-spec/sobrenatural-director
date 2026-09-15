package com.sobrenaturaldirector.execution;

import net.minecraft.nbt.NBTTagCompound;
import com.sobrenaturaldirector.domain.StableId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Durable parent ledger entry; child provider journals remain authoritative for world state. */
public final class CoordinatedExecutionRecord {
    private final String executionId, parentPlanId, fingerprint, status;
    private final List<String> completedSlices;
    public CoordinatedExecutionRecord(String executionId, String parentPlanId, String fingerprint, String status) {
        this.executionId = StableId.require(executionId, "executionId"); this.parentPlanId = StableId.require(parentPlanId, "parentPlanId");
        this.fingerprint = StableId.require(fingerprint, "fingerprint"); this.status = StableId.require(status, "status");
        this.completedSlices = Collections.emptyList();
    }
    private CoordinatedExecutionRecord(String fingerprint, String executionId, String parentPlanId, String status, List<String> completedSlices) {
        this.executionId = StableId.require(executionId, "executionId"); this.parentPlanId = StableId.require(parentPlanId, "parentPlanId");
        this.fingerprint = StableId.require(fingerprint, "fingerprint"); this.status = StableId.require(status, "status");
        this.completedSlices = Collections.unmodifiableList(new ArrayList<String>(completedSlices));
    }
    public String getExecutionId() { return executionId; }
    public String getParentPlanId() { return parentPlanId; }
    public String getFingerprint() { return fingerprint; }
    public String getStatus() { return status; }
    public List<String> getCompletedSlices() { return completedSlices; }
    public CoordinatedExecutionRecord withStatus(String value) { return new CoordinatedExecutionRecord(fingerprint, executionId, parentPlanId, value, completedSlices); }
    public CoordinatedExecutionRecord withSliceCompleted(String sliceId) { List<String> values = new ArrayList<String>(completedSlices); if (!values.contains(sliceId)) values.add(sliceId); return new CoordinatedExecutionRecord(fingerprint, executionId, parentPlanId, "EXECUTING", values); }
    public boolean matches(MultiProviderAuthorization authorization, String currentFingerprint) { return authorization != null && parentPlanId.equals(authorization.getParentPlanId()) && fingerprint.equals(currentFingerprint) && fingerprint.equals(authorization.getPlanFingerprint()); }
    public NBTTagCompound toNbt() { NBTTagCompound nbt = new NBTTagCompound(); nbt.setString("executionId", executionId); nbt.setString("parentPlanId", parentPlanId); nbt.setString("fingerprint", fingerprint); nbt.setString("status", status); net.minecraft.nbt.NBTTagList slices = new net.minecraft.nbt.NBTTagList(); for (String slice : completedSlices) { NBTTagCompound value = new NBTTagCompound(); value.setString("id", slice); slices.appendTag(value); } nbt.setTag("completedSlices", slices); return nbt; }
    public static CoordinatedExecutionRecord fromNbt(NBTTagCompound nbt) { List<String> slices = new ArrayList<String>(); if (nbt.hasKey("completedSlices", 9)) { net.minecraft.nbt.NBTTagList values = nbt.getTagList("completedSlices", 10); for (int i = 0; i < values.tagCount(); i++) slices.add(values.getCompoundTagAt(i).getString("id")); } return new CoordinatedExecutionRecord(nbt.getString("fingerprint"), nbt.getString("executionId"), nbt.getString("parentPlanId"), nbt.getString("status"), slices); }
}
