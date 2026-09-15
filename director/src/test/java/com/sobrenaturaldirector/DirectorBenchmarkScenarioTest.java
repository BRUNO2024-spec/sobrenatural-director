package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import com.sobrenaturaldirector.benchmark.DirectorBenchmarkScenario;

public final class DirectorBenchmarkScenarioTest {
    private DirectorBenchmarkScenario scenario(long seed) { Map<String,String> state=new LinkedHashMap<String,String>(); state.put("family", "RECOVERY_REQUIRED"); state.put("split", "TRAIN"); return new DirectorBenchmarkScenario("scenario-"+seed, seed, 0, "0:1:2", "WILDERNESS", state); }
    @Test public void scenarioIsImmutable() { DirectorBenchmarkScenario s=scenario(1); assertEquals("RECOVERY_REQUIRED", s.getState().get("family")); try { s.getState().put("x", "y"); fail(); } catch (UnsupportedOperationException expected) { } }
    @Test public void sameScenarioFingerprintIsStable() { assertEquals(scenario(1).fingerprint(), scenario(1).fingerprint()); }
    @Test public void differentSeedsDoNotLeakFingerprint() { assertNotEquals(scenario(1).fingerprint(), scenario(2).fingerprint()); }
    @Test public void dimensionIsPartOfScenario() { assertEquals(0, scenario(1).getDimension()); assertEquals("0:1:2", scenario(1).getRegionKey()); }
    @Test public void environmentIsExplicit() { assertEquals("WILDERNESS", scenario(1).getEnvironmentClassification()); }
    @Test public void emptyStateIsSupported() { assertTrue(new DirectorBenchmarkScenario("empty", 4, -1, "-1:0:0", "UNKNOWN", Collections.<String,String>emptyMap()).getState().isEmpty()); }
    @Test public void idsAreRequired() { try { new DirectorBenchmarkScenario("", 1, 0, "r", "WILDERNESS", null); fail(); } catch (IllegalArgumentException expected) { } }
    @Test public void scenarioDataIsCopied() { Map<String,String> state=new LinkedHashMap<String,String>(); state.put("x", "1"); DirectorBenchmarkScenario s=new DirectorBenchmarkScenario("copy", 1, 0, "r", "UNKNOWN", state); state.put("x", "2"); assertEquals("1", s.getState().get("x")); }
}
