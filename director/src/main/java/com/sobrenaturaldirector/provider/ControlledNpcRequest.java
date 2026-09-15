package com.sobrenaturaldirector.provider;

import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.domain.StableId;
import com.sobrenaturaldirector.mutation.runtime.MutationOperation;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;

/** Narrow, structured request. It cannot carry arbitrary provider data or NBT. */
public final class ControlledNpcRequest {
    private final String mutationId, planId, requestSource, logicalId, displayName, origin, expectedVersion;
    private final ProviderId providerId;
    private final MutationOperation operation;
    private final DirectorRuntimeMode mode;
    private final WorldServer world;
    private final int dimension, x, y, z;
    private final boolean authorized;

    public ControlledNpcRequest(String mutationId, String planId, String requestSource, ProviderId providerId,
            MutationOperation operation, WorldServer world, int dimension, int x, int y, int z,
            String logicalId, String displayName, String origin, String expectedVersion,
            DirectorRuntimeMode mode, boolean authorized) {
        this.mutationId = StableId.require(mutationId, "mutationId");
        this.planId = StableId.require(planId, "planId");
        this.requestSource = StableId.require(requestSource, "requestSource");
        if (providerId == null || operation == null) throw new IllegalArgumentException("provider and operation are required");
        if (mode == null) throw new IllegalArgumentException("mode is required");
        this.providerId = providerId; this.operation = operation; this.mode = mode; this.world = world;
        this.dimension = dimension; this.x = x; this.y = y; this.z = z;
        this.logicalId = StableId.require(logicalId, "logicalId");
        this.displayName = validName(displayName);
        this.origin = StableId.require(origin, "origin");
        this.expectedVersion = StableId.require(expectedVersion, "expectedVersion");
        this.authorized = authorized;
    }
    private static String validName(String value) {
        String name = StableId.require(value, "displayName");
        if (name.length() > 64) throw new IllegalArgumentException("displayName too long");
        for (int i = 0; i < name.length(); i++) if (Character.isISOControl(name.charAt(i))) throw new IllegalArgumentException("displayName contains control character");
        return name;
    }
    public String getMutationId() { return mutationId; }
    public String getPlanId() { return planId; }
    public String getRequestSource() { return requestSource; }
    public ProviderId getProviderId() { return providerId; }
    public MutationOperation getOperation() { return operation; }
    public DirectorRuntimeMode getMode() { return mode; }
    public WorldServer getWorld() { return world; }
    public int getDimension() { return dimension; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public String getLogicalId() { return logicalId; }
    public String getDisplayName() { return displayName; }
    public String getOrigin() { return origin; }
    public String getExpectedVersion() { return expectedVersion; }
    public boolean isAuthorized() { return authorized; }
}
