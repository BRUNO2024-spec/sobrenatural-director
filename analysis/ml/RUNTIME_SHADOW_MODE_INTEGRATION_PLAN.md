# Runtime Shadow Mode integration plan

## Boundaries

The deterministic boundary is `decision.DecisionEngine.decide(...)`: it sorts
and hard-filters `CandidateAction` values, evaluates deterministic utility, and
creates `DecisionResult`. The shadow hook belongs immediately after the
immutable pre-decision candidate/context copy is captured and before planning;
it must never feed a value back into `DecisionEngine`, `CandidatePlan`, or the
executor. The execution boundary remains the existing
`DirectorRuntimeCoordinator`/controlled execution path.

## Design

`ShadowDecisionSnapshot` contains only immutable primitive/string DTO values.
`ShadowObservationService` owns a bounded queue and asynchronous worker. Model
loading is fail-closed against export/config/schema hashes. The writer emits
local JSONL segments with size and total-disk limits. Queue overflow, model
failure, writer failure, and shutdown are telemetry-only failures.

The actual Director choice is recorded from `DecisionResult`; disagreement is
not a quality label. Outcome records contain objective lifecycle facts only,
with TTL and incomplete-session markers. No reward or player-preference label
is assigned. Default shadow is disabled and no network telemetry exists.

Validation will use pure Java harnesses for decision invariance, slow-model
non-blocking behavior, model/collector failure safety, snapshot immutability,
bounded resources, and lifecycle restart. No Forge/world access is allowed in
the worker.
