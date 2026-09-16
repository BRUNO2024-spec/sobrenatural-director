package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import com.sobrenaturaldirector.research.SessionConsentRegistry;
import org.junit.Test;

public class SessionConsentRegistryTest {
    @Test public void consentIsSessionScopedAndOpaque() {
        SessionConsentRegistry r=new SessionConsentRegistry(); r.join("in-memory-key");
        assertEquals(SessionConsentRegistry.State.PENDING,r.state("in-memory-key"));
        String id=r.accept("in-memory-key"); assertTrue(id.startsWith("participant-")); assertFalse(id.contains("in-memory-key"));
        assertEquals(id,r.participantId("in-memory-key")); r.disconnect("in-memory-key");
        assertEquals(SessionConsentRegistry.State.PENDING,r.state("in-memory-key")); assertNull(r.participantId("in-memory-key"));
    }
    @Test public void declineAndRevokeSuppressParticipant() {
        SessionConsentRegistry r=new SessionConsentRegistry(); r.accept("a"); r.revoke("a"); assertEquals(SessionConsentRegistry.State.DECLINED,r.state("a")); assertNull(r.participantId("a"));
        r.join("b"); r.decline("b"); assertNull(r.participantId("b"));
    }
}
