package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import com.sobrenaturaldirector.observation.model.EnvironmentSnapshot;
import com.sobrenaturaldirector.observation.model.InventorySlotSnapshot;
import com.sobrenaturaldirector.observation.model.LatestObservationStore;
import com.sobrenaturaldirector.observation.model.ObservationDiagnostics;
import com.sobrenaturaldirector.observation.model.ObservationFrame;
import com.sobrenaturaldirector.observation.model.ObservationStatus;
import com.sobrenaturaldirector.observation.model.PlayerSnapshot;
import com.sobrenaturaldirector.observation.model.WorldSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.Test;

public class ObservationModelTest {
    private static EnvironmentSnapshot environment() { return new EnvironmentSnapshot(4, "plains", 10, true, 64.0); }
    private static PlayerSnapshot player(String value, double x) { return new PlayerSnapshot(UUID.fromString(value), "display", 0, x, 64.0, -3.0, 18.0f, 20.0f, 20, 2, 5, false, true, environment(), Arrays.asList(new InventorySlotSnapshot(2, "minecraft:stone", 3, 0, 0, false, false, "MAIN"), new InventorySlotSnapshot(0, InventorySlotSnapshot.UNKNOWN_REGISTRY, 1, 0, 0, false, true, "HOTBAR"))); }
    private static ObservationFrame frame() { return new ObservationFrame("capture:1:0", 0, 1, Arrays.asList(new WorldSnapshot(1, 5, 10, 2, false, false, 1), new WorldSnapshot(0, 5, 10, 2, false, false, 1)), Arrays.asList(player("00000000-0000-0000-0000-000000000002", 2), player("00000000-0000-0000-0000-000000000001", -2)), new ObservationDiagnostics(ObservationStatus.COMPLETE, 2, 2, Collections.<String>emptyList())); }

    @Test public void emptyFrameIsValid() { ObservationFrame value = new ObservationFrame("empty", 0, 0, Collections.<WorldSnapshot>emptyList(), Collections.<PlayerSnapshot>emptyList(), new ObservationDiagnostics(ObservationStatus.COMPLETE, 0, 0, Collections.<String>emptyList())); assertTrue(value.getPlayers().isEmpty()); }
    @Test public void worldsAreSortedByDimension() { assertEquals(0, frame().getWorlds().get(0).getDimensionId()); }
    @Test public void playersAreSortedByUuid() { assertEquals("00000000-0000-0000-0000-000000000001", frame().getPlayers().get(0).getPlayerId().toString()); }
    @Test public void negativeCoordinatesAreValid() { assertEquals(-2.0, frame().getPlayers().get(0).getX(), 0.0); }
    @Test public void inventoryIsSortedAndImmutable() { PlayerSnapshot value=frame().getPlayers().get(0); assertEquals(0, value.getInventory().get(0).getSlotIndex()); try { value.getInventory().clear(); throw new AssertionError(); } catch (UnsupportedOperationException expected) { } }
    @Test public void unknownRegistryIsExplicit() { assertEquals("UNKNOWN", frame().getPlayers().get(0).getInventory().get(0).getRegistryName()); }
    @Test public void environmentIsValueData() { assertTrue(environment().canSeeSky()); assertEquals(10, environment().getBlockLight()); }
    @Test public void nanAndInfinityAreRejected() { try { player("00000000-0000-0000-0000-000000000003", Double.NaN); throw new AssertionError(); } catch (IllegalArgumentException expected) { } try { new EnvironmentSnapshot(0, "x", 1, false, Double.POSITIVE_INFINITY); throw new AssertionError(); } catch (IllegalArgumentException expected) { } }
    @Test public void frameEqualityIsSemantic() { assertEquals(frame(), frame()); }
    @Test public void latestStoreKeepsOnlyLatest() { LatestObservationStore store=new LatestObservationStore(); ObservationFrame first=frame(); store.publish(first); ObservationFrame second=new ObservationFrame("capture:2:200",200,2,first.getWorlds(),first.getPlayers(),first.getDiagnostics()); store.publish(second); assertEquals(second,store.getLatest()); assertNotSame(first,store.getLatest()); store.clear(); assertFalse(store.getLatest()!=null); }
}
