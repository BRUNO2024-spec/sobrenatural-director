package com.sobrenaturaldirector.mutation.runtime;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;

public final class MutationExecutionPolicy {
    private MutationExecutionPolicy() { }

    public static MutationExecutionResult.Status validate(WorldServer world, MutationExecutionRequest request,
            DirectorRuntimeMode mode, boolean enabled, DirectorWorldSavedData saved) {
        if (mode != DirectorRuntimeMode.CONTROLLED_EXECUTION) return MutationExecutionResult.Status.REJECTED_MODE;
        if (!enabled) return MutationExecutionResult.Status.REJECTED_DISABLED;
        if (world == null || request == null || saved == null) return MutationExecutionResult.Status.REJECTED_POLICY;
        if (request.getOperation() != MutationOperation.PLACE_BLOCK) return MutationExecutionResult.Status.REJECTED_UNSUPPORTED_OPERATION;
        if (request.getDimension() != 0 || world.provider.dimensionId != 0) return MutationExecutionResult.Status.REJECTED_DIMENSION;
        if (request.getY() < 0 || request.getY() >= 256) return MutationExecutionResult.Status.REJECTED_INVALID_Y;
        if (saved.hasExecutedMutation(request.getMutationId())) return MutationExecutionResult.Status.ALREADY_EXECUTED;
        if (!world.getChunkProvider().chunkExists(request.getX() >> 4, request.getZ() >> 4))
            return MutationExecutionResult.Status.REJECTED_CHUNK_NOT_LOADED;
        Block target = Block.getBlockFromName(request.getBlockName());
        if (target == null || target != Block.getBlockFromName("minecraft:glass"))
            return MutationExecutionResult.Status.REJECTED_UNSUPPORTED_BLOCK;
        if (!world.isAirBlock(request.getX(), request.getY(), request.getZ()))
            return MutationExecutionResult.Status.REJECTED_BLOCK_NOT_AIR;
        if (!"minecraft:air".equals(request.getExpectedBlock()))
            return MutationExecutionResult.Status.REJECTED_POLICY;
        if (world.getTileEntity(request.getX(), request.getY(), request.getZ()) != null)
            return MutationExecutionResult.Status.REJECTED_TILE_ENTITY;
        if (!world.getBlock(request.getX(), request.getY() - 1, request.getZ()).getMaterial().isSolid())
            return MutationExecutionResult.Status.REJECTED_POLICY;
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(request.getX(), request.getY(), request.getZ(),
                request.getX() + 1, request.getY() + 1, request.getZ() + 1);
        for (Object value : world.getEntitiesWithinAABB(Entity.class, box)) {
            Entity entity = (Entity) value;
            if (entity instanceof EntityLivingBase || entity.canBeCollidedWith())
                return MutationExecutionResult.Status.REJECTED_ENTITY_OCCUPIED;
        }
        return MutationExecutionResult.Status.EXECUTED;
    }
}
