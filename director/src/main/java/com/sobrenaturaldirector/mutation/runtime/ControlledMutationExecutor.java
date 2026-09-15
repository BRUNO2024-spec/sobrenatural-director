package com.sobrenaturaldirector.mutation.runtime;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;

/** Server-thread executor for the explicitly controlled Phase 1K path. */
public final class ControlledMutationExecutor {
    private final DirectorRuntimeMode mode;
    private int requests, policyRejected, executed, failed, duplicatesBlocked, rollbacks, rollbackFailures;

    public ControlledMutationExecutor(DirectorRuntimeMode mode) { this.mode = mode == null ? DirectorRuntimeMode.DRY_RUN : mode; }

    public MutationExecutionResult execute(WorldServer world, DirectorWorldSavedData saved,
            MutationExecutionRequest request, boolean enabled) {
        requests++;
        MutationExecutionResult.Status status = MutationExecutionPolicy.validate(world, request, mode, enabled, saved);
        if (status == MutationExecutionResult.Status.ALREADY_EXECUTED) { duplicatesBlocked++; log("mutation REJECTED id=%s result=ALREADY_EXECUTED", request.getMutationId()); return MutationExecutionResult.of(status, request.getMutationId()); }
        if (status != MutationExecutionResult.Status.EXECUTED) { policyRejected++; log("mutation REJECTED id=%s result=%s", request == null ? "null" : request.getMutationId(), status); return MutationExecutionResult.of(status, request == null ? "" : request.getMutationId()); }
        Block before = world.getBlock(request.getX(), request.getY(), request.getZ());
        if (!world.isAirBlock(request.getX(), request.getY(), request.getZ())) return result(MutationExecutionResult.Status.ABORT_STALE_PRECONDITION, request);
        MutationJournalEntry journal = new MutationJournalEntry(request.getMutationId(), request.getPlanId(), request.getSourceId(), request.getOperation().name(),
                request.getDimension(), request.getX(), request.getY(), request.getZ(), name(before), world.getBlockMetadata(request.getX(), request.getY(), request.getZ()),
                request.getBlockName(), request.getMetadata(), "PREPARED", world.getTotalWorldTime(), null, "NOT_REQUESTED");
        saved.recordMutation(journal);
        try {
            boolean changed = world.setBlock(request.getX(), request.getY(), request.getZ(), Block.getBlockFromName(request.getBlockName()), request.getMetadata(), 3);
            if (!changed || !request.getBlockName().equals(name(world.getBlock(request.getX(), request.getY(), request.getZ())))) {
                saved.updateMutation(journal.withStatus("FAILED", "NOT_REQUESTED", world.getTotalWorldTime()));
                return result(MutationExecutionResult.Status.FAILED_WORLD_WRITE, request);
            }
            saved.updateMutation(journal.withStatus("EXECUTED", "NOT_REQUESTED", world.getTotalWorldTime()));
            executed++; log("mutation EXECUTED id=%s operation=%s target=%d,%d,%d before=%s after=%s", request.getMutationId(), request.getOperation(), request.getX(), request.getY(), request.getZ(), name(before), request.getBlockName());
            return result(MutationExecutionResult.Status.EXECUTED, request);
        } catch (RuntimeException failure) {
            saved.updateMutation(journal.withStatus("FAILED", "NOT_REQUESTED", world.getTotalWorldTime()));
            failed++; log("mutation FAILED id=%s", request.getMutationId());
            return result(MutationExecutionResult.Status.FAILED_WORLD_WRITE, request);
        }
    }

    public MutationExecutionResult rollback(WorldServer world, DirectorWorldSavedData saved, String mutationId) {
        MutationJournalEntry entry = saved == null ? null : saved.getMutation(mutationId);
        if (entry == null) { rollbackFailures++; return MutationExecutionResult.of(MutationExecutionResult.Status.ROLLBACK_NOT_FOUND, mutationId); }
        if (!"EXECUTED".equals(entry.getStatus())) { rollbackFailures++; return MutationExecutionResult.of(MutationExecutionResult.Status.REJECTED_POLICY, mutationId); }
        if (world == null || world.provider.dimensionId != entry.getDimension() || !world.getChunkProvider().chunkExists(entry.getX() >> 4, entry.getZ() >> 4)) { rollbackFailures++; return MutationExecutionResult.of(MutationExecutionResult.Status.REJECTED_POLICY, mutationId); }
        if (!entry.getAfterBlock().equals(name(world.getBlock(entry.getX(), entry.getY(), entry.getZ()))) || world.getBlockMetadata(entry.getX(), entry.getY(), entry.getZ()) != entry.getAfterMetadata()) {
            saved.updateMutation(entry.withStatus("EXECUTED", "ROLLBACK_STALE_STATE", world.getTotalWorldTime())); rollbackFailures++; return MutationExecutionResult.of(MutationExecutionResult.Status.ROLLBACK_STALE_STATE, mutationId);
        }
        if (!world.setBlock(entry.getX(), entry.getY(), entry.getZ(), Blocks.air, 0, 3)) { rollbackFailures++; return MutationExecutionResult.of(MutationExecutionResult.Status.FAILED_WORLD_WRITE, mutationId); }
        saved.updateMutation(entry.withStatus("ROLLED_BACK", "EXECUTED", world.getTotalWorldTime())); rollbacks++; log("mutation rollback id=%s result=ROLLBACK_EXECUTED", mutationId);
        return MutationExecutionResult.of(MutationExecutionResult.Status.ROLLBACK_EXECUTED, mutationId);
    }

    private MutationExecutionResult result(MutationExecutionResult.Status status, MutationExecutionRequest request) { return MutationExecutionResult.of(status, request.getMutationId()); }
    private static String name(Block block) { Object value = Block.blockRegistry.getNameForObject(block); return value == null ? "unknown" : value.toString(); }
    private static void log(String format, Object... args) { FMLLog.log("sobrenaturaldirector", Level.INFO, format, args); }
    public int getRequests() { return requests; }
    public int getPolicyRejected() { return policyRejected; }
    public int getExecuted() { return executed; }
    public int getFailed() { return failed; }
    public int getDuplicatesBlocked() { return duplicatesBlocked; }
    public int getRollbacks() { return rollbacks; }
    public int getRollbackFailures() { return rollbackFailures; }
}
