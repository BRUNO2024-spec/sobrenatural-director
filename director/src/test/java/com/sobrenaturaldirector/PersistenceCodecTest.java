package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.sobrenaturaldirector.domain.DirectorWorldState;
import com.sobrenaturaldirector.domain.DirectorWorldState.CleanupTask;
import com.sobrenaturaldirector.domain.DirectorWorldState.ContentUsageRecord;
import com.sobrenaturaldirector.domain.DirectorWorldState.CooldownRecord;
import com.sobrenaturaldirector.domain.DirectorWorldState.DirectorEventState;
import com.sobrenaturaldirector.domain.DirectorWorldState.DirectorPlanState;
import com.sobrenaturaldirector.domain.DirectorWorldState.DirectorPlayerState;
import com.sobrenaturaldirector.domain.DirectorWorldState.KnownLocation;
import com.sobrenaturaldirector.domain.DirectorWorldState.MemoryRecord;
import com.sobrenaturaldirector.domain.DirectorWorldState.MutationRecord;
import com.sobrenaturaldirector.domain.DirectorWorldState.NarrativeArcState;
import com.sobrenaturaldirector.domain.DirectorWorldState.ProviderState;
import com.sobrenaturaldirector.domain.DirectorWorldState.ReservedRegion;
import com.sobrenaturaldirector.domain.DirectorWorldState.StructureRecord;
import com.sobrenaturaldirector.domain.DirectorWorldState.UniqueClaim;
import com.sobrenaturaldirector.persistence.DirectorPersistenceMetadata;
import com.sobrenaturaldirector.persistence.DirectorStateNbtCodec;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.persistence.LoadStatus;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

public class PersistenceCodecTest {
    @Test public void dataNameIsStableAndNotVersioned() {
        assertEquals("SobrenaturalDirectorWorldState", DirectorPersistenceMetadata.DATA_NAME);
        assertFalse(DirectorPersistenceMetadata.DATA_NAME.contains("0.2.0"));
    }

    @Test public void freshAdapterHasWritableEmptyState() {
        DirectorWorldSavedData data = new DirectorWorldSavedData();
        assertEquals(LoadStatus.FRESH, data.getLoadStatus());
        assertTrue(data.isWritable());
        assertEquals(DirectorPersistenceMetadata.DATA_NAME, data.mapName);
    }
    private static DirectorWorldState populated() {
        return new DirectorWorldState(987654321L, true,
            Arrays.asList(new DirectorPlayerState(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"), "DISCOVERED", 2)),
            Arrays.asList(new KnownLocation("loc-a", 0, 10, 20, 30, 1, 5, 2, "GLIMPSED", true, Arrays.asList("CAVE", "DARK"))),
            Arrays.asList(new ReservedRegion("reservation-a", "plan-a", 0, 0, 0, 0, 4, 4, 4, "MYSTERY_SITE", 3, 50)),
            Arrays.asList(new MemoryRecord("memory-a", "EVENT", "PERMANENT", 4, -1)),
            Arrays.asList(new UniqueClaim("claim-a", "resource-a", "event-a", 5, "HELD")),
            Arrays.asList(new CooldownRecord("cooldown-a", "WORLD", 6, 10)),
            Arrays.asList(new ProviderState("provider-a", "AVAILABLE", 0)),
            Arrays.asList(new ContentUsageRecord("content-a", 2, 7)),
            Arrays.asList(new CleanupTask("cleanup-a", "event-a", 1, 8)),
            Arrays.asList(new NarrativeArcState("arc-a", "THEME", "ACTIVE", 2)),
            Arrays.asList(new DirectorPlanState("plan-a", "event-a", "TENTATIVE")),
            Arrays.asList(new DirectorEventState("event-a", "DIRECTOR_EVENT", "PREPARE")),
            Arrays.asList(new StructureRecord("structure-a", "loc-a", "PLANNED")),
            Arrays.asList(new MutationRecord("mutation-a", "NONE", 9)));
    }

    @Test public void emptyAndPopulatedStatesRoundTrip() {
        assertEquals(DirectorWorldState.empty(1L), DirectorStateNbtCodec.decode(DirectorStateNbtCodec.encode(DirectorWorldState.empty(1L))));
        DirectorWorldState state = populated();
        assertEquals(state, DirectorStateNbtCodec.decode(DirectorStateNbtCodec.encode(state)));
    }

    @Test public void schemaAndStableOrderingAreWritten() {
        NBTTagCompound root = DirectorStateNbtCodec.encode(populated());
        assertEquals(DirectorPersistenceMetadata.CURRENT_SCHEMA_VERSION, root.getInteger("schemaVersion"));
        assertEquals("loc-a", root.getTagList("locations", 10).getCompoundTagAt(0).getString("id"));
        assertTrue(root.hasKey("cleanupTasks", 9));
    }

    @Test public void futureSchemaIsQuarantinedAndPreserved() {
        NBTTagCompound future = DirectorStateNbtCodec.encode(DirectorWorldState.empty(8L));
        future.setInteger("schemaVersion", 2); future.setString("futureSentinel", "MUST_SURVIVE");
        DirectorWorldSavedData data = new DirectorWorldSavedData(); data.readFromNBT(future);
        assertEquals(LoadStatus.UNSUPPORTED_FUTURE_SCHEMA, data.getLoadStatus()); assertFalse(data.isWritable());
        NBTTagCompound output = new NBTTagCompound(); data.writeToNBT(output);
        assertEquals("MUST_SURVIVE", output.getString("futureSentinel")); assertEquals(2, output.getInteger("schemaVersion"));
    }

    @Test public void oldAndMissingSchemaAreQuarantined() {
        NBTTagCompound old = new NBTTagCompound(); old.setInteger("schemaVersion", 0); old.setString("oldSentinel", "KEEP");
        DirectorWorldSavedData oldData = new DirectorWorldSavedData(); oldData.readFromNBT(old); assertEquals(LoadStatus.UNSUPPORTED_OLD_SCHEMA, oldData.getLoadStatus());
        NBTTagCompound missing = new NBTTagCompound(); missing.setString("payload", "KEEP");
        DirectorWorldSavedData missingData = new DirectorWorldSavedData(); missingData.readFromNBT(missing); assertEquals(LoadStatus.MISSING_SCHEMA, missingData.getLoadStatus());
    }

    @Test public void corruptionIsFailClosedWithoutResettingRawPayload() {
        NBTTagCompound corrupt = DirectorStateNbtCodec.encode(DirectorWorldState.empty(3L)); corrupt.setString("directorSeed", "wrong"); corrupt.setString("corruptSentinel", "KEEP");
        DirectorWorldSavedData data = new DirectorWorldSavedData(); data.readFromNBT(corrupt);
        assertEquals(LoadStatus.CORRUPT, data.getLoadStatus()); assertFalse(data.isWritable());
        NBTTagCompound output = new NBTTagCompound(); data.writeToNBT(output); assertEquals("KEEP", output.getString("corruptSentinel"));
    }

    @Test public void replaceStateMarksDirtyOnlyWhenWritable() {
        DirectorWorldSavedData data = new DirectorWorldSavedData(); data.replaceState(DirectorWorldState.empty(55L)); assertTrue(data.isDirty());
        NBTTagCompound future = DirectorStateNbtCodec.encode(DirectorWorldState.empty(1L)); future.setInteger("schemaVersion", 99); data.readFromNBT(future);
        try { data.replaceState(DirectorWorldState.empty(2L)); throw new AssertionError(); } catch (IllegalStateException expected) { }
    }
}
