package com.sobrenaturaldirector.control;

import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;

/** Server-thread-only bridge; never put this context in planning/telemetry DTOs. */
public final class ControlExecutionContext {
    private final WorldServer world; private final DirectorWorldSavedData saved;
    public ControlExecutionContext(WorldServer world, DirectorWorldSavedData saved) { if (world == null || saved == null) throw new IllegalArgumentException("runtime context"); this.world=world; this.saved=saved; }
    public WorldServer getWorld(){return world;} public DirectorWorldSavedData getSaved(){return saved;}
}
