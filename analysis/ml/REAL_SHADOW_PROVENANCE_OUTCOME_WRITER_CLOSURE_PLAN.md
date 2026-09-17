# Real Shadow provenance and outcome writer closure

## Technical cause

The V1 `JsonlShadowEventWriter` serialized only three decision events and the
V1 `SessionConsentRegistry` kept consent entirely in memory. There was no
collection-session identifier, participant-session callback, artifact
identity, lifecycle manifest, event sequence, or terminal event for
`NO_ACTION`. The existing bounded `ShadowOutcomeTracker` was not connected to
the writer or to an execution event source.

## V2 boundary

`DIRECTOR_SHADOW_EXPERIENCE_V2` writes only to
`shadow-data/v2/collection-sessions/<collectionSessionId>/`. The legacy
`shadow-data/shadow-00000.jsonl` is never opened for append and remains V1
`LEGACY_PROVENANCE_INCOMPLETE`.

Each V2 boot writes a start manifest and `COLLECTION_SESSION_START`; graceful
shutdown writes participant ends, `COLLECTION_SESSION_END`, and an end
manifest. Consent acceptance writes `PARTICIPANT_SESSION_START` immediately.
IDs are random base32-like opaque values (`cs_`, `ps_`, `p_`) and are not
derived from username, UUID, IP, or entity ID.

The V2 writer includes immutable JAR/model/config/feature identities, a
monotonic event sequence, server tick, field/dedicated provenance, and
`trainingAllowed=false`. A factual `candidate:no_action` decision receives a
terminal `DECISION_TERMINAL(NO_ACTION)`; it is not an execution success or
reward. Explicit execution lifecycle methods are available for real execution
integrations and the harness, while Learned Control remains off.

## Deliberate limits

The current dedicated configuration has no discovered optional provider and
does not enable autonomous gameplay collection. Therefore this closure does
not fabricate action executions or outcomes. The old V1 file is not
canonicalized and is not reclassified retroactively.
