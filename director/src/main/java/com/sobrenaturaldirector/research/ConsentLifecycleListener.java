package com.sobrenaturaldirector.research;

/** Receives research-safe consent lifecycle facts; no account identity is exposed. */
public interface ConsentLifecycleListener {
    void accepted(String participantSessionId, String opaqueParticipantId, long serverTick);
    void revoked(String participantSessionId, String opaqueParticipantId, long serverTick);
    void disconnected(String participantSessionId, String opaqueParticipantId, long serverTick);
    void ended(String participantSessionId, String opaqueParticipantId, String reason, long serverTick);
}
