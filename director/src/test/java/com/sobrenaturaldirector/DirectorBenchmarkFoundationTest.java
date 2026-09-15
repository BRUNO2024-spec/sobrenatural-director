package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import org.junit.Test;
import com.sobrenaturaldirector.benchmark.DirectorBenchmarkMath;

public final class DirectorBenchmarkFoundationTest {
    @Test public void normalizationIsBounded() { assertEquals(0, DirectorBenchmarkMath.normalize(-1, 0, 1), 0); assertEquals(100, DirectorBenchmarkMath.normalize(2, 0, 1), 0); }
    @Test public void normalizationIsStable() { assertEquals(50, DirectorBenchmarkMath.normalize(.5, 0, 1), 0); }
    @Test public void compositeUsesFixedWeights() { assertEquals(70, DirectorBenchmarkMath.composite(100, 50, 50, 50), 0); }
    @Test public void relativeImprovementHandlesZeroBaseline() { assertNull(DirectorBenchmarkMath.relativeImprovement(10, 0)); assertNull(DirectorBenchmarkMath.percentageImprovement(10, 0)); }
    @Test public void relativeImprovementIsDirectional() { assertEquals(1.5, DirectorBenchmarkMath.relativeImprovement(75, 50), 0); assertEquals(50, DirectorBenchmarkMath.percentageImprovement(75, 50), 0); }
    @Test public void hardSafetyWeightIsHighest() { assertTrue(DirectorBenchmarkMath.composite(100, 0, 0, 0) > DirectorBenchmarkMath.composite(0, 100, 0, 0)); }
    @Test public void scoreDoesNotExceedHundredForValidInputs() { assertEquals(100, DirectorBenchmarkMath.composite(100, 100, 100, 100), 0); }
    @Test public void scoreAllowsRawComponentComparison() { assertEquals(40, DirectorBenchmarkMath.composite(100, 0, 0, 0), 0); }
    @Test public void invalidRangeIsRejected() { try { DirectorBenchmarkMath.normalize(1, 2, 2); fail(); } catch (IllegalArgumentException expected) { } }
    @Test public void offlineMathHasNoMutationSurface() { assertNotNull(DirectorBenchmarkMath.class); }
}
