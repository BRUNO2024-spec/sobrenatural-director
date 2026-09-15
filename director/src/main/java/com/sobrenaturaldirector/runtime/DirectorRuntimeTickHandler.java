package com.sobrenaturaldirector.runtime;

import com.sobrenaturaldirector.observation.runtime.ObservationCoordinator;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;

public final class DirectorRuntimeTickHandler {
    private final ObservationCoordinator observation;
    private final DirectorRuntimeCoordinator coordinator;
    private long tickCounter;

    public DirectorRuntimeTickHandler(ObservationCoordinator observation, DirectorRuntimeCoordinator coordinator) {
        this.observation = observation;
        this.coordinator = coordinator;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        long tick = server == null ? tickCounter : tickCounter++;
        try { observation.onServerTick(server, tick); }
        catch (Throwable failure) { /* Observation is best-effort; mutation boundaries remain independently callable. */ }
        try { coordinator.onServerTick(server, tick); }
        catch (Throwable failure) { /* A failed read-only evaluation must not stop the server tick bus. */ }
    }
}
