package com.sobrenaturaldirector.naturalpressure;

import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.persistence.DirectorPersistenceMetadata;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;

/** Optional runtime surface for addon adapters. It creates no directives autonomously. */
public final class NaturalThreatPressureRuntime {
    public static final boolean AUTONOMOUS_NATURAL_PRESSURE = false;
    private NaturalThreatPressureRuntime() { }
    public static boolean isAutonomousPressureEnabled() { return AUTONOMOUS_NATURAL_PRESSURE; }
    public static NaturalThreatPressureResolution resolve(WorldServer world, String provider, String category, int dimension, int chunkX, int chunkZ, long tick) { return resolve(world, provider, category, dimension, chunkX, chunkZ, tick, true); }
    public static NaturalThreatPressureResolution resolve(WorldServer world, String provider, String category, int dimension, int chunkX, int chunkZ, long tick, boolean providerAvailable) { DirectorWorldSavedData saved = load(world); return saved == null ? new NaturalThreatPressureResolution(NaturalThreatPressureLevel.NORMAL, "", "director-unavailable", providerAvailable) : saved.resolveNaturalPressure(provider, category, dimension, chunkX, chunkZ, tick, providerAvailable); }
    public static boolean put(WorldServer world, NaturalThreatPressureDirective directive) { DirectorWorldSavedData saved = writable(world); if(saved==null)return false; saved.putNaturalPressure(directive); return true; }
    public static boolean remove(WorldServer world, String directiveId) { DirectorWorldSavedData saved = load(world); return saved != null && saved.removeNaturalPressure(directiveId); }
    private static DirectorWorldSavedData load(WorldServer world) { if(world==null)return null; return (DirectorWorldSavedData)world.perWorldStorage.loadData(DirectorWorldSavedData.class, DirectorPersistenceMetadata.DATA_NAME); }
    private static DirectorWorldSavedData writable(WorldServer world) { if(world==null)return null; DirectorWorldSavedData saved=load(world); if(saved==null){saved=new DirectorWorldSavedData();world.perWorldStorage.setData(DirectorPersistenceMetadata.DATA_NAME,saved);}return saved; }
}
