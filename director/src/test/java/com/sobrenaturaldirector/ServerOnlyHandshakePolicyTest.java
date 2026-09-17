package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import cpw.mods.fml.common.Mod;
import org.junit.Test;

/** Regression for Forge 1.7.10 remote-mod negotiation. */
public class ServerOnlyHandshakePolicyTest {
    @Test public void clientsWithoutDirectorAreAcceptedByRemotePolicy() {
        Mod metadata = SobrenaturalDirector.class.getAnnotation(Mod.class);
        assertEquals("*", metadata.acceptableRemoteVersions());
    }
}
