package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.sobrenaturaldirector.mutation.runtime.MutationExecutionRequest;
import com.sobrenaturaldirector.mutation.runtime.MutationExecutionResult;
import com.sobrenaturaldirector.mutation.runtime.MutationJournalEntry;
import com.sobrenaturaldirector.mutation.runtime.MutationOperation;

public final class ControlledMutationModelTest {
    @Test public void requestAndJournalRoundTrip() {
        MutationExecutionRequest request = new MutationExecutionRequest("mutation-a", "plan-a", "CONTROLLED_VALIDATION",
                MutationOperation.PLACE_BLOCK, 0, 10, 65, 20, "minecraft:glass", 0, "minecraft:air");
        MutationJournalEntry entry = new MutationJournalEntry(request.getMutationId(), request.getPlanId(), request.getSourceId(),
                request.getOperation().name(), request.getDimension(), request.getX(), request.getY(), request.getZ(),
                "minecraft:air", 0, request.getBlockName(), request.getMetadata(), "EXECUTED", 12, null, "NOT_REQUESTED");
        MutationJournalEntry decoded = MutationJournalEntry.fromNbt(entry.toNbt());
        assertEquals(entry.getMutationId(), decoded.getMutationId());
        assertEquals(entry.getPlanId(), decoded.getPlanId());
        assertEquals(entry.getAfterBlock(), decoded.getAfterBlock());
        assertEquals(entry.getStatus(), decoded.getStatus());
        assertEquals(entry.getX(), decoded.getX());
    }

    @Test public void resultKeepsStructuredStatus() {
        MutationExecutionResult result = MutationExecutionResult.of(MutationExecutionResult.Status.REJECTED_DISABLED, "mutation-a");
        assertEquals(MutationExecutionResult.Status.REJECTED_DISABLED, result.getStatus());
        assertEquals("mutation-a", result.getMutationId());
    }
}
