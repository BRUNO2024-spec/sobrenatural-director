package com.sobrenaturaldirector.provider.slenderman;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.threat.ThreatExecutionRequest;
import com.sobrenaturaldirector.threat.ThreatOwnership;
import com.sobrenaturaldirector.threat.PersistentNarrativeThreat;

/** Lazy, version-pinned reflection boundary for the optional SlenderMan mod. */
final class SlenderManRuntimeBridge {
    static final String MOD_ID = "dg_slender";
    static final String VERSION = "3.3_1.7.10";
    private static final String OWNER_CLASS = "net.dudgames.slender.common.SlenderMan";
    private static final String ENTITY_CLASS = "net.dudgames.slender.entity.EntitySlenderMan";
    private static final String REGISTRY_NAME = "Slender Man";
    private final Constructor<?> constructor;
    private final Class<?> entityClass;
    private final Field canSpawn;
    private final ReentrantLock gateLock = new ReentrantLock();

    SlenderManRuntimeBridge() {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> owner = Class.forName(OWNER_CLASS, false, loader);
            entityClass = Class.forName(ENTITY_CLASS, false, loader);
            canSpawn = owner.getDeclaredField("canSpawn");
            if (!Modifier.isStatic(canSpawn.getModifiers()) || canSpawn.getType() != Boolean.TYPE) throw new IllegalStateException("SlenderMan.canSpawn contract mismatch");
            canSpawn.setAccessible(true);
            constructor = entityClass.getConstructor(net.minecraft.world.World.class);
            if (!isRegistered()) throw new IllegalStateException("Slender Man registry/class contract mismatch");
        } catch (Exception failure) { throw new IllegalStateException("SlenderMan bridge initialization failed", failure); }
    }

    Boolean openStartupGate() { Boolean previous = changeGate(true); if (previous != null) FMLLog.log("sobrenaturaldirector", Level.INFO, "SlenderMan canSpawn GATE_OPEN stage=RELOAD original=%s", previous); return previous; }
    void closeStartupGate(boolean original) { restoreGate(original); FMLLog.log("sobrenaturaldirector", Level.INFO, "SlenderMan canSpawn GATE_CLOSE stage=RELOAD restored=%s", original); }

    Entity create(final ThreatExecutionRequest request, final int[] site, final boolean injectFailure) {
        try {
            return withGate(new Callable<Entity>() { public Entity call() throws Exception {
                if (injectFailure) throw new IllegalStateException("test fault injection inside canSpawn gate");
                Entity entity = (Entity) constructor.newInstance(request.getWorld());
                if (!entityClass.isInstance(entity)) throw new IllegalStateException("factory returned unexpected entity class");
                entity.setPosition(site[0] + 0.5D, site[1], site[2] + 0.5D);
                ((EntityLiving) entity).func_110163_bv();
                return entity;
            }});
        } catch (Exception failure) { throw new IllegalStateException("SlenderMan controlled creation failed", failure); }
    }

    private Entity withGate(Callable<Entity> operation) throws Exception {
        gateLock.lock();
        Boolean previous = changeGate(true);
        if (previous == null) { gateLock.unlock(); throw new IllegalStateException("SlenderMan.canSpawn contract unavailable"); }
        FMLLog.log("sobrenaturaldirector", Level.INFO, "SlenderMan canSpawn GATE_OPEN stage=OPERATION original=%s", previous);
        try { return operation.call(); }
        finally { try { restoreGate(previous.booleanValue()); FMLLog.log("sobrenaturaldirector", Level.INFO, "SlenderMan canSpawn GATE_CLOSE restored=%s", previous); } finally { gateLock.unlock(); } }
    }

    private Boolean changeGate(boolean value) { try { boolean previous = canSpawn.getBoolean(null); canSpawn.setBoolean(null, value); return Boolean.valueOf(previous); } catch (IllegalAccessException failure) { return null; } }
    private void restoreGate(boolean expected) {
        Boolean previous = changeGate(expected);
        if (previous == null || canSpawnValue() != expected) throw new IllegalStateException("SlenderMan.canSpawn restore failed");
    }
    private boolean canSpawnValue() { try { return canSpawn.getBoolean(null); } catch (IllegalAccessException failure) { throw new IllegalStateException("SlenderMan.canSpawn read failed", failure); } }

    boolean isTarget(Entity entity) { return entity != null && entityClass.isInstance(entity) && ENTITY_CLASS.equals(entity.getClass().getName()); }
    boolean isRegistered() {
        for (Field field : EntityList.class.getDeclaredFields()) try {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true); Object value = field.get(null);
            if (value instanceof Map && entityClass.equals(((Map<?, ?>) value).get(REGISTRY_NAME))) return true;
        } catch (Exception ignored) { }
        return false;
    }

    boolean safeSite(WorldServer world, int x, int y, int z) {
        if (world == null || y < 2 || y >= 253 || !world.getChunkProvider().chunkExists(x >> 4, z >> 4)) return false;
        if (!world.isAirBlock(x, y, z) || !world.isAirBlock(x, y + 1, z) || !world.isAirBlock(x, y + 2, z)) return false;
        Block support = world.getBlock(x, y - 1, z);
        if (support == null || !support.getMaterial().isSolid() || support.getMaterial().isLiquid()) return false;
        if (world.getTileEntity(x, y, z) != null || world.getTileEntity(x, y + 1, z) != null || world.getTileEntity(x, y + 2, z) != null) return false;
        return !world.checkBlockCollision(AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 3, z + 1));
    }

    int[] findSafeSite(WorldServer world, int x, int y, int z) {
        int spawnChunkX = world.getSpawnPoint().posX >> 4, spawnChunkZ = world.getSpawnPoint().posZ >> 4;
        int chunkX = x >> 4, chunkZ = z >> 4;
        if (chunkX != spawnChunkX || chunkZ != spawnChunkZ) return null;
        for (int dx = -8; dx <= 8; dx++) for (int dz = -8; dz <= 8; dz++) for (int dy = -8; dy <= 8; dy++) {
            int candidateX = x + dx, candidateY = y + dy, candidateZ = z + dz;
            if ((candidateX >> 4) != chunkX || (candidateZ >> 4) != chunkZ) continue;
            if (safeSite(world, candidateX, candidateY, candidateZ)) return new int[] { candidateX, candidateY, candidateZ };
        }
        return null;
    }

    void mark(Entity entity, ThreatExecutionRequest request, String threatInstanceId) {
        NBTTagCompound data = entity.getEntityData();
        data.setString(ThreatOwnership.MANAGED_BY, ThreatOwnership.MANAGED_BY_VALUE);
        data.setString(ThreatOwnership.ORIGIN, ThreatOwnership.ORIGIN_VALUE);
        data.setString(ThreatOwnership.PROVIDER_ID, request.getProviderId().getValue());
        data.setString(ThreatOwnership.REQUEST_ID, request.getRequestId());
        data.setString(ThreatOwnership.THREAT_INSTANCE_ID, threatInstanceId);
        data.setString(ThreatOwnership.NARRATIVE_ID, "narrative-threat:" + request.getRequestId());
        data.setString(ThreatOwnership.DEFINITION_ID, request.getDefinitionId());
    }

    boolean owns(Entity entity, ThreatExecutionRequest request, String threatInstanceId) {
        if (!isTarget(entity) || entity.isDead) return false;
        NBTTagCompound data = entity.getEntityData();
        return ThreatOwnership.MANAGED_BY_VALUE.equals(data.getString(ThreatOwnership.MANAGED_BY))
                && ThreatOwnership.ORIGIN_VALUE.equals(data.getString(ThreatOwnership.ORIGIN))
                && request.getProviderId().getValue().equals(data.getString(ThreatOwnership.PROVIDER_ID))
                && request.getRequestId().equals(data.getString(ThreatOwnership.REQUEST_ID))
                && threatInstanceId.equals(data.getString(ThreatOwnership.THREAT_INSTANCE_ID))
                && request.getDefinitionId().equals(data.getString(ThreatOwnership.DEFINITION_ID));
    }

    Entity find(WorldServer world, UUID uuid) { if (world == null || uuid == null) return null; for (Object value : world.loadedEntityList) if (value instanceof Entity && uuid.equals(((Entity) value).getUniqueID())) return (Entity) value; return null; }

    boolean owns(Entity entity, PersistentNarrativeThreat threat) {
        if (!isTarget(entity) || entity.isDead || threat == null) return false;
        NBTTagCompound data = entity.getEntityData();
        return ThreatOwnership.MANAGED_BY_VALUE.equals(data.getString(ThreatOwnership.MANAGED_BY))
                && threat.getProviderId().getValue().equals(data.getString(ThreatOwnership.PROVIDER_ID))
                && threat.getRequestId().equals(data.getString(ThreatOwnership.REQUEST_ID))
                && threat.getId().equals(data.getString(ThreatOwnership.NARRATIVE_ID));
    }
}
