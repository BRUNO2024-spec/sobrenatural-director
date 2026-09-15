package com.sobrenaturaldirector.provider.customnpcs;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.provider.ControlledNpcRequest;
import com.sobrenaturaldirector.provider.DirectorEntityMetadata;

/** Version-pinned bridge. Every reflected symbol is a compile-time allowlisted constant. */
final class CustomNpcsRuntimeBridge {
    private static final String ENTITY_CLASS = "noppes.npcs.entity.EntityCustomNpc";
    private static final String NPC_ID = DirectorEntityMetadata.NPC_ID;
    private static final String PROVIDER = DirectorEntityMetadata.PROVIDER;
    private static final String ORIGIN = DirectorEntityMetadata.ORIGIN;
    private static final String MUTATION = DirectorEntityMetadata.MUTATION;
    private final Class<?> entityClass;
    private final Constructor<?> constructor;
    private final Method delete;
    private final Field display;
    private final Field displayName;

    CustomNpcsRuntimeBridge() {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            entityClass = Class.forName(ENTITY_CLASS, false, loader);
            constructor = entityClass.getConstructor(net.minecraft.world.World.class);
            delete = entityClass.getMethod("delete");
            display = entityClass.getField("display");
            displayName = display.getType().getField("name");
        } catch (Exception failure) { throw new IllegalStateException("CustomNPCs supported bridge initialization failed: " + failure, failure); }
    }

    UUID create(ControlledNpcRequest request) {
        try {
            Entity entity = (Entity) constructor.newInstance(request.getWorld());
            entity.setLocationAndAngles(request.getX() + 0.5D, (double) request.getY(), request.getZ() + 0.5D, 0.0F, 0.0F);
            Object displayData = display.get(entity);
            displayName.set(displayData, request.getDisplayName());
            NBTTagCompound data = entity.getEntityData();
            data.setString(NPC_ID, request.getLogicalId());
            data.setBoolean(DirectorEntityMetadata.MANAGED, true);
            data.setString(PROVIDER, "customnpcs");
            data.setString(ORIGIN, request.getOrigin());
            data.setString(MUTATION, request.getMutationId());
            if (!request.getWorld().spawnEntityInWorld(entity)) throw new IllegalStateException("CustomNPC world insertion rejected");
            return entity.getUniqueID();
        } catch (Exception failure) { throw new IllegalStateException("CustomNPC controlled creation failed", failure); }
    }

    UUID remove(ControlledNpcRequest request) {
        try {
            Entity target = find(request.getWorld(), request.getMutationId());
            if (target == null) return null;
            UUID uuid = target.getUniqueID();
            delete.invoke(target);
            target.setDead();
            request.getWorld().removeEntity(target);
            return uuid;
        } catch (Exception failure) { throw new IllegalStateException("CustomNPC controlled removal failed", failure); }
    }

    private Entity find(WorldServer world, String mutationId) throws Exception {
        for (Object value : world.loadedEntityList) if (entityClass.isInstance(value)) {
            Entity entity = (Entity) value;
            if (entity.isDead) continue;
            NBTTagCompound data = entity.getEntityData();
            if ("customnpcs".equals(data.getString(PROVIDER)) && "DIRECTOR_NPC".equals(data.getString(ORIGIN))
                    && mutationId.equals(data.getString(MUTATION))) return entity;
        }
        return null;
    }
}
