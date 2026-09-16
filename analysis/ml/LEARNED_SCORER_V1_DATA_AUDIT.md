# Learned Scorer V1 data audit

## Data flow found

`DirectorBenchmarkRunner` is the sole writer of the tracked JSONL exports. Its
`run()` method creates scenarios, calls `candidates(scenario)`, passes the
candidate list to `CurrentHeuristicDirectorPolicy` (an offline adapter around
`DecisionEngine`), and then writes details and features. `action()` constructs
candidate metadata before the policy call. `decide()` reads the selected
candidate after the call and calculates the V1 quality value.

```text
deterministic Scenario -> context + all CandidateAction objects
                         -> heuristic DecisionEngine -> selected candidate
                         -> details (selected/outcome/quality/latency)
                         -> V1 features (selected row only)
                         -> V2 candidate rows (pre-decision inputs + separate target)
```

The benchmark is plan-only and does not call Minecraft APIs, spawn entities,
load worlds, or execute MPE actions.

## Leakage and representation

V1 writes one row per decision, not one row per candidate. Its
`candidateFeatures.quality` is copied from the post-selection `Result.quality`,
and the detail quality is exactly the same value (1200/1200 rows). Thus a model
given V1 candidate features receives the target verbatim. V1 `decision` is also
identical to `policyDecision` and cannot be an input for predicting it.

There are multiple candidates before selection: `candidates()` always creates
ambient and threat candidates, optionally continuation and no-action. V1 loses
all unselected candidates. V2 retains every candidate and places `target.quality`
and `target.chosen` outside `candidateFeatures`.

## Field timing

Pre-decision inputs: scenario seed/split metadata, deterministic state
(`hardValid`, no-action requirement, environment, dimension, provider
availability, continuation opportunity, recent-high count), and candidate
identity/kind, intent, safety, provider and base utility. These are constructed
before `policy.evaluate()`.

Post-decision or prohibited inputs: selected candidate, policy decision,
selected blueprint/intent/provider/intensity, final quality, latency, and any
future execution outcome. They remain in V1 details only or are V2 targets.

## Risks and recommendation

The current benchmark has synthetic, heuristic-derived supervision and no
independent human/outcome label. V2 therefore supports a small regression
scorer and candidate ranking only as a baseline; it does not establish product
quality or replace deterministic safety. Keep the scorer in shadow mode and
fit normalization/vocabularies on TRAIN only. TEST and HOLDOUT remain locked.
