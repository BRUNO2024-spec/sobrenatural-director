package com.sobrenaturaldirector.observation.runtime;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.server.MinecraftServer;

public final class ObservationTickHandler {
    private long tickCounter;
    private final ObservationCoordinator coordinator;
    public ObservationTickHandler(ObservationCoordinator coordinator) { if (coordinator == null) throw new IllegalArgumentException("coordinator is required"); this.coordinator=coordinator; }
    @SubscribeEvent public void onServerTick(TickEvent.ServerTickEvent event) { if (event.phase == TickEvent.Phase.END) { MinecraftServer server=cpw.mods.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance(); coordinator.onServerTick(server, tickCounter++); } }
}
