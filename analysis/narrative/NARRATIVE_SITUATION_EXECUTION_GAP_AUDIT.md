# Narrative Situation Execution Gap Audit

## Baseline

The existing `SituationIntelligencePipeline` evaluated context, selected a goal,
ranked a blueprint, resolved semantic capabilities, and produced a
`CandidatePlan`. It was explicitly plan-only. `ActionPlanExecutor` was already
the sole concrete mutation boundary.

## Findings

1. Goal ended at `SituationDecision.selected` and `CandidatePlan`.
2. Blueprint ended at `BlueprintCandidate`; no executable instance existed.
3. CandidatePlan was not converted to an `ActionPlan`.
4. ActionPlan began only in the independent semantic action stack.
5. No runtime `SituationInstance` or validated situation state machine existed.
6. Narrative threads and persistent plans existed, but had no situation identity
   or factual action-outcome link.
7. Memory recorded bounded planning observations, not terminal situation facts,
   and accepted duplicate IDs.
8. Dimension was present in context/plans but not represented as situation origin
   and relevant dimensions.
9. Restart-safe situation state and situation journal linkage were absent.

## Closure boundary

`SituationComposer` now creates one immutable, opaque `SituationInstance`,
references the shared `ActionPlan`, and enters `READY` only after deterministic
composition. `SituationExecutionService` is an explicit caller-owned bridge to
`ActionPlanExecutor`; it rechecks dimension and maps factual `ActionOutcome`
to lifecycle without allowing narrative code to call providers directly.

`SituationLifecycle` is fail-closed. Memory IDs are idempotent. Public bootstrap
remains plan-only and no autonomous entrypoint invokes this service.

## Remaining intentional scope

Persistent NBT serialization of the new situation ledger and full scheduler
activation remain disabled until a dedicated runtime journal is added. The
public server therefore remains `AUTONOMOUS_EXECUTION=OFF`.
