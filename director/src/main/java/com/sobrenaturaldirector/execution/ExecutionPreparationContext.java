package com.sobrenaturaldirector.execution;

import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.situation.SituationContext;
import com.sobrenaturaldirector.situation.SituationBlueprint;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.domain.StableId;

/** Immutable, provider-neutral inputs used to prepare executable slices. */
public final class ExecutionPreparationContext {
    private final WorldServer world;
    private final DirectorWorldSavedData saved;
    private final SituationContext situation;
    private final SituationBlueprint blueprint;
    private final SituationMemory memory;
    private final int anchorX, anchorY, anchorZ;
    private final String executionId;
    public ExecutionPreparationContext(WorldServer world, DirectorWorldSavedData saved, SituationContext situation,
            SituationBlueprint blueprint, SituationMemory memory, int anchorX, int anchorY, int anchorZ) {
        this(world, saved, situation, blueprint, memory, anchorX, anchorY, anchorZ, "prepared-execution");
    }
    public ExecutionPreparationContext(WorldServer world, DirectorWorldSavedData saved, SituationContext situation,
            SituationBlueprint blueprint, SituationMemory memory, int anchorX, int anchorY, int anchorZ, String executionId) {
        this.world = world; this.saved = saved; this.situation = situation; this.blueprint = blueprint; this.memory = memory;
        this.anchorX = anchorX; this.anchorY = anchorY; this.anchorZ = anchorZ;
        this.executionId = StableId.require(executionId, "executionId");
    }
    public WorldServer getWorld() { return world; }
    public DirectorWorldSavedData getSaved() { return saved; }
    public SituationContext getSituation() { return situation; }
    public SituationBlueprint getBlueprint() { return blueprint; }
    public SituationMemory getMemory() { return memory; }
    public int getAnchorX() { return anchorX; } public int getAnchorY() { return anchorY; } public int getAnchorZ() { return anchorZ; }
    public String getExecutionId() { return executionId; }
}
