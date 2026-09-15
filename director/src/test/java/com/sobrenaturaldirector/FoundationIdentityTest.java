package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class FoundationIdentityTest {
    @Test public void identityIsCentralized() {
        assertEquals("sobrenaturaldirector", SobrenaturalDirector.MOD_ID);
        assertEquals("Sobrenatural Director", SobrenaturalDirector.MOD_NAME);
        assertEquals("0.12.0-alpha", SobrenaturalDirector.VERSION);
        assertEquals("1.7.10", SobrenaturalDirector.MINECRAFT_VERSION);
        assertEquals("10.13.4.1614", SobrenaturalDirector.TARGET_FORGE_VERSION);
    }
}
