# Shadow runtime safety analysis

The deterministic Director remains the only authority. Shadow receives copied
DTOs, runs on a daemon worker, and returns telemetry only. It cannot provide a
`DecisionResult`, `CandidatePlan`, execution request, provider selection, or
safety result. Submission uses a bounded non-blocking queue; model, writer,
disk, and queue failures are dropped or counted while gameplay continues.

Hard safety, authorization, preflight, rollback, idempotency, and NO_ACTION
remain deterministic. Disagreement is not a label; completion is not player
satisfaction; health loss is not narrative quality.
