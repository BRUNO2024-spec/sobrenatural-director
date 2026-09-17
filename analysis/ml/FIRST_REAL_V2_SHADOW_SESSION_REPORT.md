# First real V2 Shadow session report

## Classification

`CLASSIFICATION=PASS_FIRST_REAL_V2_SHADOW_SESSION`

The FIELD session is `cs_YEG6Z4FUQ6YX`. Operational logs independently show
an external join, approximately 22 minutes of gameplay, and disconnect. The
research file itself contains no player identity.

## Session and consent

The collection session has a valid `COLLECTION_SESSION_START` and an active
collection session (no `COLLECTION_SESSION_END` yet; the server is still
running). It contains one `PARTICIPANT_SESSION_START` with
`consentState=ACCEPTED` and `startReason=CONSENT_ACCEPTED`, followed by one
`PARTICIPANT_SESSION_END` with `endReason=DISCONNECT`.

The participant session spans 26382 server ticks, approximately 1319.1
seconds (21m 59.1s at 20 ticks/second). No decision record precedes the
participant start.

## Event and decision counts

| Event/type | Count |
|---|---:|
| COLLECTION_SESSION_START | 1 |
| COLLECTION_SESSION_END | 0 (session still active) |
| PARTICIPANT_SESSION_START | 1 |
| PARTICIPANT_SESSION_END | 1 |
| DECISION_SNAPSHOT | 132 |
| ACTUAL_DECISION | 132 |
| SHADOW_SCORE_RESULT | 132 |
| DECISION_TERMINAL(NO_ACTION) | 132 |
| execution events | 0 |

All 132 decision IDs have a complete snapshot/actual/shadow triplet. The
actual choice was `candidate:no_action` 132 times; the Shadow choice was the
same 132 times. This is reported as heuristic/Shadow agreement, not accuracy.

Candidate count was exactly one for every decision. Thus actual rank-1 was
132/132 and score margin is unavailable (there was no second candidate).
Inference latency was 0.0370605 ms p50, 0.060552 ms p95, 0.928861 ms p99,
with maximum 2.485341 ms.

## Outcomes and quality tiers

All 132 decisions have a factual terminal `NO_ACTION` resolution. None has an
execution outcome, because no ACTION decision occurred. Therefore:

- `TIER_A_NO_ACTION=132`
- `TIER_A_EXECUTION=0`
- `TIER_B=0`
- `TIER_C=0`
- terminal decision coverage: 100%
- complete execution outcome count: 0
- counterfactual outcome: unavailable

`NO_ACTION` is not labeled success, failure, reward, or preference.

## Integrity and privacy

The event file has 531 valid UTF-8 JSON lines, zero parse errors, zero
duplicates, zero orphan decision events, and event sequence 1..531 with no
gaps. All records use V2 and the same collection session.

JAR identity matches `e3ede9ebda12cf213fc2de324998bfa09f69c91f4b26836a3f5932bb0a30dd53`.
Model identity matches `dd4ccebad2b7f94db74a27493c2418e7abdea5ee66509d39e2a3982538592af9`.
Frozen config and feature-schema identities match the boot manifest.

Privacy scan passed: no account UUID, offline UUID, username, IP, chat, or
public server address was found. Opaque IDs use the expected `cs_`, `ps_`, and
`p_` forms.

The immutable RAW import has the same SHA-256 as the server event file:
`ed4465431d51e8fe61fd4bf167b060d8815648c4c0132ba2dd9de1bd7825b429`.
Canonical output contains 132 descriptive records with
`trainingAllowed=false`; it is not a reward or training dataset.

Queue/scorer/writer counters are not persisted in this session's event
schema. No such error event was observed, but this is reported as
`UNKNOWN_NOT_EXPOSED`, not silently converted to zero.

## Field progress

This is one valid V2 real session with 132 scored decisions and 132 terminal
decision resolutions. Field validation remains
`INSUFFICIENT_REAL_SESSIONS`: two additional valid real sessions are required.
The historical V1 session is excluded.
