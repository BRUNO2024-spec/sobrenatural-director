# V2 field telemetry and NO_ACTION trace closure

## Cause addressed

The V2 writer previously exposed only decision counts. Queue drops, scorer
requests/errors, queue high-watermark and writer/session health remained in
memory or were absent from the end manifest. The decision path also submitted
only eligible candidates to Shadow, so the historical `candidateCount=1` did
not distinguish a genuinely singleton candidate set from action candidates
rejected before enqueue.

## Observation-only design

`ShadowDecisionDiagnostics` is an immutable summary calculated after the same
candidate constraints and utility scores used by `DecisionEngine`. It records
total/action/NO_ACTION candidates, eligible action candidates, rejection
categories, provider availability counts and factual reason codes. It is
attached to V2 events and cannot select, score, reorder, or execute anything.

`ShadowObservationMetrics` uses atomics for enqueue/drop/high-watermark and
scorer request/success/error counters. A bounded `HEALTH_SNAPSHOT` is emitted
at startup and every 100 scored decisions; the final collection event carries
the health summary. Existing V1 and historical V2 files are not rewritten.

The live status script reads the current V2 health snapshot without exposing
username, UUID, IP, or chat. Missing historical health remains
`UNKNOWN_NOT_EXPOSED`; it is not backfilled as zero.

## Behavioral invariants

No heuristic, threshold, provider, safety gate, model, weight, autonomy mode,
or pacing behavior was changed. Existing tests and the synthetic V2 harness
continue to validate decision invariance, immutable diagnostics, lifecycle,
privacy, and `trainingAllowed=false`.
