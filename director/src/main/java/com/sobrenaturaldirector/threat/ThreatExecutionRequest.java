package com.sobrenaturaldirector.threat;

import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.domain.StableId;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;

public final class ThreatExecutionRequest {
    private final String requestId, source, origin, expectedVersion;
    private final ProviderId providerId;
    private final String definitionId;
    private final DirectorRuntimeMode mode;
    private final WorldServer world;
    private final int dimension, x, y, z;
    private final boolean authorized;

    public ThreatExecutionRequest(String requestId, String source, ProviderId providerId, String definitionId,
            String origin, String expectedVersion, DirectorRuntimeMode mode, WorldServer world,
            int dimension, int x, int y, int z, boolean authorized) {
        this.requestId = StableId.require(requestId, "requestId");
        this.source = StableId.require(source, "source");
        if (providerId == null) throw new IllegalArgumentException("providerId is required");
        this.providerId = providerId;
        this.definitionId = StableId.require(definitionId, "definitionId");
        this.origin = StableId.require(origin, "origin");
        this.expectedVersion = StableId.require(expectedVersion, "expectedVersion");
        if (mode == null) throw new IllegalArgumentException("mode is required");
        this.mode = mode; this.world = world; this.dimension = dimension; this.x = x; this.y = y; this.z = z; this.authorized = authorized;
    }
    public String getRequestId() { return requestId; }
    public String getSource() { return source; }
    public ProviderId getProviderId() { return providerId; }
    public String getDefinitionId() { return definitionId; }
    public String getOrigin() { return origin; }
    public String getExpectedVersion() { return expectedVersion; }
    public DirectorRuntimeMode getMode() { return mode; }
    public WorldServer getWorld() { return world; }
    public int getDimension() { return dimension; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public boolean isAuthorized() { return authorized; }
}
