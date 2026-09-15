package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.sobrenaturaldirector.config.DebugLevel;
import com.sobrenaturaldirector.config.FoundationConfig;
import org.junit.Test;

public class FoundationConfigTest {
    @Test public void defaultsAreSafe() {
        assertTrue(FoundationConfig.defaults().isEnabled());
        assertEquals(DebugLevel.INFO, FoundationConfig.defaults().getDebugLevel());
        assertTrue(FoundationConfig.defaults().isObservationEnabled());
        assertEquals(200, FoundationConfig.defaults().getObservationIntervalTicks());
        assertFalse(FoundationConfig.defaults().isExecutionEnabled());
        assertEquals(1, FoundationConfig.CURRENT_CONFIG_VERSION);
    }

    @Test public void invalidDebugLevelFallsBack() {
        assertEquals(DebugLevel.INFO, DebugLevel.parse("not-a-level"));
        assertEquals(DebugLevel.INFO, DebugLevel.parse(null));
        assertEquals(DebugLevel.VERBOSE, DebugLevel.parse("verbose"));
    }

}
