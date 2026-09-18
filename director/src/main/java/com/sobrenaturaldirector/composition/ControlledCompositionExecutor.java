package com.sobrenaturaldirector.composition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import com.sobrenaturaldirector.composition.model.CompositionBlockOperation;
import com.sobrenaturaldirector.composition.model.CompositionExecutionResult;
import com.sobrenaturaldirector.composition.model.DirectorOwnedCompositionPlan;
import com.sobrenaturaldirector.composition.model.ExternalBlockReference;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;

/** Controlled composition boundary. Provider access is by registry identity only. */
public final class ControlledCompositionExecutor {
    private final DirectorRuntimeMode mode;
    private final ExternalBlockPolicy policy;
    public ControlledCompositionExecutor(DirectorRuntimeMode mode, ExternalBlockPolicy policy) {
        this.mode = mode == null ? DirectorRuntimeMode.DRY_RUN : mode;
        if (policy == null) throw new IllegalArgumentException("external block policy is required");
        this.policy = policy;
    }

    public CompositionExecutionResult execute(WorldServer world, DirectorWorldSavedData saved,
            DirectorOwnedCompositionPlan plan, boolean enabled) {
        if (plan == null) return result(CompositionExecutionResult.Status.PREFLIGHT_REJECTED, "", 0);
        if (mode != DirectorRuntimeMode.CONTROLLED_EXECUTION) return result(CompositionExecutionResult.Status.REJECTED_MODE, plan.getCompositionId(), 0);
        if (!enabled) return result(CompositionExecutionResult.Status.REJECTED_DISABLED, plan.getCompositionId(), 0);
        CompositionJournalEntry existing = saved == null ? null : saved.getComposition(plan.getCompositionId());
        if (existing != null && "EXECUTED".equals(existing.getStatus())) return result(CompositionExecutionResult.Status.ALREADY_EXECUTED, plan.getCompositionId(), 0);
        CompositionExecutionResult.Status preflight = preflight(world, saved, plan);
        if (preflight != CompositionExecutionResult.Status.EXECUTED) return result(preflight, plan.getCompositionId(), 0);
        List<CompositionJournalEntry.Child> children = new ArrayList<CompositionJournalEntry.Child>();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (CompositionBlockOperation op : plan.getOperations()) {
            int x=plan.getAnchorX()+op.getRelativeX(), y=plan.getAnchorY()+op.getRelativeY(), z=plan.getAnchorZ()+op.getRelativeZ();
            ExternalBlockReference ref=op.getBlock(); String name=ref.getProviderModId()+":"+ref.getRegistryName();
            children.add(new CompositionJournalEntry.Child(op.getChildMutationId(), name, ref.getMetadata(), x,y,z));
            minX=Math.min(minX,x); minY=Math.min(minY,y); minZ=Math.min(minZ,z); maxX=Math.max(maxX,x); maxY=Math.max(maxY,y); maxZ=Math.max(maxZ,z);
        }
        CompositionJournalEntry journal = new CompositionJournalEntry(plan.getCompositionId(), plan.getSource(), plan.getProviderModId(), plan.getProviderVersion(), "PREPARED", plan.getDimension(), plan.getAnchorX(), plan.getAnchorY(), plan.getAnchorZ(), minX,minY,minZ,maxX,maxY,maxZ,world.getTotalWorldTime(),children);
        saved.recordComposition(journal);
        int writes=0;
        try {
            for (CompositionJournalEntry.Child child : children) {
                Block block=Block.getBlockFromName(child.block);
                if (!world.setBlock(child.x,child.y,child.z,block,child.metadata,3) || !child.block.equals(name(world.getBlock(child.x,child.y,child.z)))) throw new IllegalStateException("world write rejected");
                writes++;
            }
            saved.updateComposition(journal.withStatus("EXECUTED",world.getTotalWorldTime()));
            log("composition EXECUTED id=%s blocks=%d bounds=%d,%d,%d..%d,%d,%d",plan.getCompositionId(),writes,minX,minY,minZ,maxX,maxY,maxZ);
            return result(CompositionExecutionResult.Status.EXECUTED,plan.getCompositionId(),writes);
        } catch (RuntimeException failure) {
            for (int i=writes-1;i>=0;i--) { CompositionJournalEntry.Child child=children.get(i); if (name(world.getBlock(child.x,child.y,child.z)).equals(child.block)) world.setBlock(child.x,child.y,child.z,Blocks.air,0,3); }
            saved.updateComposition(journal.withStatus("FAILED_ROLLED_BACK",world.getTotalWorldTime()));
            return result(CompositionExecutionResult.Status.PARTIAL_EXECUTION_FAILURE,plan.getCompositionId(),writes);
        }
    }

    public CompositionExecutionResult rollback(WorldServer world, DirectorWorldSavedData saved, String compositionId) {
        CompositionJournalEntry entry=saved==null?null:saved.getComposition(compositionId);
        if (entry==null) { log("composition rollback missing id=%s", compositionId); return result(CompositionExecutionResult.Status.ROLLBACK_NOT_FOUND,compositionId,0); }
        if (!"EXECUTED".equals(entry.getStatus())) { log("composition rollback status id=%s status=%s", compositionId, entry.getStatus()); return result(CompositionExecutionResult.Status.ROLLBACK_NOT_FOUND,compositionId,0); }
        if (world==null || world.provider.dimensionId!=entry.getDimension()) { log("composition rollback dimension mismatch id=%s", compositionId); return result(CompositionExecutionResult.Status.ROLLBACK_REJECTED_STALE,compositionId,0); }
        for (CompositionJournalEntry.Child child:entry.getChildren()) {
            if (!child.block.equals(name(world.getBlock(child.x,child.y,child.z))) || world.getBlockMetadata(child.x,child.y,child.z)!=child.metadata) { log("composition rollback stale id=%s child=%s expected=%s:%d actual=%s:%d", compositionId, child.id, child.block, child.metadata, name(world.getBlock(child.x,child.y,child.z)), world.getBlockMetadata(child.x,child.y,child.z)); saved.updateComposition(entry.withStatus("EXECUTED",world.getTotalWorldTime())); return result(CompositionExecutionResult.Status.ROLLBACK_REJECTED_STALE,compositionId,0); }
        }
        int writes=0; for (CompositionJournalEntry.Child child:entry.getChildren()) if (world.setBlock(child.x,child.y,child.z,Blocks.air,0,3)) writes++;
        saved.updateComposition(entry.withStatus("ROLLED_BACK",world.getTotalWorldTime()));
        return result(CompositionExecutionResult.Status.ROLLBACK_EXECUTED,compositionId,writes);
    }

    private CompositionExecutionResult.Status preflight(WorldServer world, DirectorWorldSavedData saved, DirectorOwnedCompositionPlan plan) {
        if (world==null || saved==null) return CompositionExecutionResult.Status.PREFLIGHT_REJECTED;
        if (plan.getOperations().size()>DirectorOwnedCompositionPlan.MAX_BLOCKS) return CompositionExecutionResult.Status.REJECTED_TOO_MANY_BLOCKS;
        if (plan.getDimension()!=world.provider.dimensionId) return CompositionExecutionResult.Status.REJECTED_PROVIDER_UNSUPPORTED;
        if (!policy.isProviderAvailable()) return CompositionExecutionResult.Status.REJECTED_PROVIDER_UNAVAILABLE;
        Set<String> targets=new HashSet<String>();
        for (CompositionBlockOperation op:plan.getOperations()) {
            com.sobrenaturaldirector.composition.model.ExternalBlockReference ref=op.getBlock();
            if (!policy.accepts(plan.getProviderModId(), plan.getProviderVersion(), ref)) return CompositionExecutionResult.Status.REJECTED_CONTENT_OWNER;
            String name=ref.getProviderModId()+":"+ref.getRegistryName(); Block block=Block.getBlockFromName(name); if (block==null) return CompositionExecutionResult.Status.REJECTED_CONTENT_NOT_FOUND;
            int x=plan.getAnchorX()+op.getRelativeX(),y=plan.getAnchorY()+op.getRelativeY(),z=plan.getAnchorZ()+op.getRelativeZ(); String target=x+":"+y+":"+z; if (!targets.add(target)) return CompositionExecutionResult.Status.REJECTED_DUPLICATE_TARGET;
            if (y<1 || y>=256) return CompositionExecutionResult.Status.REJECTED_INVALID_Y; if (!world.getChunkProvider().chunkExists(x>>4,z>>4)) return CompositionExecutionResult.Status.REJECTED_CHUNK_NOT_LOADED;
            if (!world.isAirBlock(x,y,z)) return CompositionExecutionResult.Status.REJECTED_BLOCK_NOT_AIR; if (world.getTileEntity(x,y,z)!=null || block.hasTileEntity(ref.getMetadata())) return CompositionExecutionResult.Status.REJECTED_TILE_ENTITY;
            if (!world.getBlock(x,y-1,z).getMaterial().isSolid()) return CompositionExecutionResult.Status.REJECTED_SUPPORT;
            AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+1,z+1); for(Object value:world.getEntitiesWithinAABB(Entity.class,box)){Entity entity=(Entity)value;if(entity instanceof EntityLivingBase||entity.canBeCollidedWith())return CompositionExecutionResult.Status.REJECTED_ENTITY_OCCUPIED;}
        }
        return CompositionExecutionResult.Status.EXECUTED;
    }
    private CompositionExecutionResult result(CompositionExecutionResult.Status status,String id,int writes){return new CompositionExecutionResult(status,id,writes);}
    private static String name(Block block){Object value=Block.blockRegistry.getNameForObject(block);return value==null?"unknown":value.toString();}
    private static void log(String format,Object...args){FMLLog.log("sobrenaturaldirector",Level.INFO,format,args);}
}
