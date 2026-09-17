# V2 field telemetry and NO_ACTION trace closure

## Result

`CLASSIFICATION=PASS_V2_FIELD_TELEMETRY_NO_ACTION_TRACE_CLOSURE`

The runtime now emits immutable diagnostics calculated after the existing
decision evaluation, plus numeric queue/scorer/writer health snapshots. No
decision behavior, V4 artifact, provider, threshold, safety rule, or autonomy
setting was changed.

## Diagnostics

V2 decision events now include a bounded `decisionDiagnostics` object with:

- total, action, and NO_ACTION candidate counts;
- eligible action and rejected candidate counts;
- safety versus feasibility rejection counts;
- provider availability counts;
- factual failed constraint codes;
- explicit `selectionReasonCode`.

Goal/blueprint details are explicitly `NOT_CAPTURED` where the decision layer
does not invoke those planners; they are not inferred or fabricated. Planning
and execution are not collapsed.

## Health

Atomic metrics cover queue enqueue/drop/high-watermark and scorer
request/success/error counts. `HEALTH_SNAPSHOT` is emitted at boot and every
100 scored decisions. `COLLECTION_SESSION_END` includes the available health
summary, writer errors, model-runtime error count, and outcome tracker
placeholder counters. Historical sessions remain unchanged and continue to
report `UNKNOWN_NOT_EXPOSED` for absent health fields.

## Validation

- Java: 312 tests pass.
- Python: pass.
- V2 harness: 1000 synthetic decisions.
- Diagnostic DTO immutable; no World/Entity crosses the async boundary.
- Privacy scanner: pass.
- V1 and historical V2 files: not appended or rewritten.
- New boot session: `cs_M2QY5UMKMCFQ`, empty before player join, health
  counters numeric zero.

The server is running and ready for the second real enhanced-telemetry field
session. The previous real V2 session remains the only valid field session;
the new empty boot is not counted as gameplay.
