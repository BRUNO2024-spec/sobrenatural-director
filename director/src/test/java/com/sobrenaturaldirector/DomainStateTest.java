package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import com.sobrenaturaldirector.domain.DirectorWorldState;
import com.sobrenaturaldirector.domain.DirectorWorldState.KnownLocation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.Test;

public class DomainStateTest {
    @Test public void emptyStateIsValidAndImmutable() {
        DirectorWorldState state = DirectorWorldState.empty(42L);
        assertEquals(42L, state.getDirectorSeed());
        assertTrue(state.getKnownLocations().isEmpty());
        try { state.getKnownLocations().add(null); throw new AssertionError(); }
        catch (UnsupportedOperationException expected) { }
    }

    @Test public void constructorDefensivelyCopiesTags() {
        ArrayList<String> tags = new ArrayList<String>(Arrays.asList("CAVE"));
        KnownLocation location = new KnownLocation("loc", 0, 1, 2, 3, 0, 1, 1, "DISCOVERED", false, tags);
        tags.add("CHANGED");
        assertEquals(Collections.singletonList("CAVE"), location.tags);
        assertNotSame(tags, location.tags);
    }

    @Test public void uuidAndStableIdBoundariesAreValidated() {
        UUID id = UUID.randomUUID();
        assertEquals(id, new DirectorWorldState.DirectorPlayerState(id, "UNKNOWN", 0).playerId);
        try { new DirectorWorldState.DirectorPlayerState(id, "UNKNOWN", -1); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
        try { new KnownLocation("bad\nvalue", 0, 0, 0, 0, 0, 0, 0, "UNKNOWN", false, Collections.<String>emptyList()); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
    }

    @Test public void duplicateIdsAreRejected() {
        KnownLocation first = new KnownLocation("same", 0, 0, 0, 0, 0, 0, 0, "UNKNOWN", false, Collections.<String>emptyList());
        KnownLocation second = new KnownLocation("same", 0, 0, 0, 0, 0, 0, 0, "UNKNOWN", false, Collections.<String>emptyList());
        try { new DirectorWorldState(0, false, Collections.<DirectorWorldState.DirectorPlayerState>emptyList(), Arrays.asList(first, second), Collections.<DirectorWorldState.ReservedRegion>emptyList(), Collections.<DirectorWorldState.MemoryRecord>emptyList(), Collections.<DirectorWorldState.UniqueClaim>emptyList(), Collections.<DirectorWorldState.CooldownRecord>emptyList(), Collections.<DirectorWorldState.ProviderState>emptyList(), Collections.<DirectorWorldState.ContentUsageRecord>emptyList(), Collections.<DirectorWorldState.CleanupTask>emptyList(), Collections.<DirectorWorldState.NarrativeArcState>emptyList(), Collections.<DirectorWorldState.DirectorPlanState>emptyList(), Collections.<DirectorWorldState.DirectorEventState>emptyList(), Collections.<DirectorWorldState.StructureRecord>emptyList(), Collections.<DirectorWorldState.MutationRecord>emptyList()); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
    }

    @Test public void invalidLifecycleIsRejected() {
        try { new DirectorWorldState.NarrativeArcState("arc", "THEME", "NOT_A_STATE", 0); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
    }

    @Test public void safetyLimitsRejectOversizedInputs() {
        KnownLocation location = new KnownLocation("loc", 0, 0, 0, 0, 0, 0, 0, "UNKNOWN", false, Collections.<String>emptyList());
        try { new DirectorWorldState(0, false, Collections.<DirectorWorldState.DirectorPlayerState>emptyList(), Collections.nCopies(1025, location), Collections.<DirectorWorldState.ReservedRegion>emptyList(), Collections.<DirectorWorldState.MemoryRecord>emptyList(), Collections.<DirectorWorldState.UniqueClaim>emptyList(), Collections.<DirectorWorldState.CooldownRecord>emptyList(), Collections.<DirectorWorldState.ProviderState>emptyList(), Collections.<DirectorWorldState.ContentUsageRecord>emptyList(), Collections.<DirectorWorldState.CleanupTask>emptyList(), Collections.<DirectorWorldState.NarrativeArcState>emptyList(), Collections.<DirectorWorldState.DirectorPlanState>emptyList(), Collections.<DirectorWorldState.DirectorEventState>emptyList(), Collections.<DirectorWorldState.StructureRecord>emptyList(), Collections.<DirectorWorldState.MutationRecord>emptyList()); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
        StringBuilder oversized = new StringBuilder(); for (int i = 0; i < 129; i++) oversized.append('x');
        try { new KnownLocation(oversized.toString(), 0, 0, 0, 0, 0, 0, 0, "UNKNOWN", false, Collections.<String>emptyList()); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
    }
}
