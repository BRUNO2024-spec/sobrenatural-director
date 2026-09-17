# First real Shadow gameplay collection audit

Run: `first-real-shadow-gameplay-collection-20260917`

## Classification

`CLASSIFICATION=FAIL_FIRST_REAL_SHADOW_GAMEPLAY_COLLECTION_PIPELINE`

The server log proves external gameplay activity and the RAW segment contains
real-time decision events, but the current writer does not persist the
session/consent lifecycle required by the collection contract. The segment is
therefore retained as quarantined observational evidence, not promoted to the
canonical corpus.

## Operational evidence

- Server remains healthy: PID `2939710`, listener `*:25565`, model `READY`.
- The operational log contains two external login/join/leave cycles during the
  observed gameplay window. No player identity is copied into research data.
- RAW file: `shadow-00000.jsonl`, 111537 bytes,
  SHA-256 `7422bf5af3562926d2bff20d80774797fe21e1ccb0b39f7cc0c049b6ba83220e`.

## RAW event audit

The file parses as UTF-8 JSONL with 426 valid lines and no malformed lines.
It contains 142 `DECISION_SNAPSHOT`, 142 `ACTUAL_DECISION`, and 142
`SHADOW_SCORE_RESULT` events. All 142 decision IDs join one-to-one; ticks are
monotonic in the observed segment (`1073354..1102554`).

All actual and shadow choices are `candidate:no_action` (142 each), with
142 agreements and 0 disagreements. This is an observation only; it is not a
quality judgment. Inference latency is 0.0371405 ms p50, 0.084418 ms p95,
and 0.921838 ms p99.

No outcome, execution, session-start/end, consent, participant, queue, or
error fields are present in the RAW schema. Consequently outcome coverage is
not measurable and no factual outcome is assigned to any decision.

## Consent, provenance, and privacy

Consent acceptance cannot be confirmed from the research artifacts or the
operational log. `PRECONSENT_RECORD_COUNT` and `POSTCONSENT_RECORD_COUNT` are
therefore `UNKNOWN`, not zero. The implementation audit confirms that the
writer emits no consent/session metadata. This is the blocking defect.

The automated privacy scan found no IPv4, IPv6, UUID, username, chat payload,
server address, or sensitive filesystem path in the RAW research file.

## Storage decision

The RAW bytes were copied hash-preservingly to:
`~/.local/share/director-ml/real-experience/raw/shadow-00000/`.
An external manifest records the source hash and marks the import
`REAL_SERVER_SEGMENT_UNCONFIRMED_CONSENT`; no canonical records were created.
No training, reward labeling, normalization, or model change occurred.

## Readiness

`FIRST_REAL_SESSION_PIPELINE_STATUS=FAIL`

The first gameplay evidence is useful for diagnosing runtime activity, but it
does not satisfy the first-session gate because consent, session identity,
model/JAR per-session identity, and outcomes are absent. Field validation is
also blocked. The server may continue running, but further collection should
not be treated as validated research collection until provenance and consent
metadata are emitted atomically with each session.
