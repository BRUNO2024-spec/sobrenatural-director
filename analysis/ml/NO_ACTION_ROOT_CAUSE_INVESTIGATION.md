# NO_ACTION root-cause investigation

## Conclusion

`NO_ACTION_ROOT_CAUSE=PROVEN`

The observed decision path is not the narrative `SituationContext` → goal →
blueprint pipeline. Production tick evaluation calls
`DirectorRuntimeCoordinator.evaluate()` and then `DecisionEngine.decide()`.
The shadow snapshot is created after candidate generation and eligibility
filtering. The separate autonomous planning scheduler is called afterwards.

For every enhanced decision in M2 (40) and HIO (166), the persisted candidate
set was exactly one candidate:

```
candidateCount=1
actionCandidateCount=0
eligibleActionCandidateCount=0
noActionCandidateCount=1
selectionReasonCode=NO_ACTION_ONLY_CANDIDATE
```

The exact stop stage is:

`ACTION_PIPELINE_STOP_STAGE=CANDIDATE_GENERATION_RETURNED_ZERO_ACTIONS`

This is before feasibility, safety, heuristic ranking, and execution.

## Why generation returned zero ACTION candidates

`CandidateGenerator.generate(DecisionContext)` always adds
`candidate:no_action`, then gates action candidates as follows:

- `candidate:hint`: `tension > 0.25`;
- `candidate:minor`: health ratio, `HORROR=AVAILABLE`, and
  `COMMON_THREAT` content tag;
- `candidate:ambush`: tension, underground, `AMBUSHER`, `T3_HIGH`, and health
  gates;
- `candidate:boss`: tension, combat power, `BOSS_ROOM`, `UNIQUE_BOSS`, `BOSS`,
  and event concurrency gates;
- `candidate:recovery`: `recoveryNeed > 0.45`.

The production `DecisionContextAssembler` used by the observed path supplies
literal zero/empty values for tension, pressure, fatigue, recovery need,
isolation, event concurrency, providers, content tags, and location tags. It
only maps health ratio, combat readiness, history claims, and cooldowns.
Therefore the provider-independent `hint` and `recovery` gates are false, and
the provider/content-backed gates are also false. This proves the elimination
at candidate generation, without requiring a provider or safety rejection.

## Required fields

```
TRACE_COVERAGE=PARTIAL
GOAL_SELECTED=UNKNOWN_NOT_CAPTURED
GOAL_SATISFIABLE=NOT_CAPTURED
REQUIRED_CAPABILITIES=NOT_CAPTURED_IN_DECISION_PATH
AVAILABLE_CAPABILITIES=NOT_CAPTURED_IN_DECISION_PATH
MISSING_CAPABILITIES=NOT_CAPTURED
BLUEPRINT_CANDIDATES=NOT_CAPTURED
FEASIBLE_BLUEPRINTS=NOT_CAPTURED
RAW_ACTION_CANDIDATES=0_PER_DECISION
ACTION_AFTER_FEASIBILITY=0
ACTION_AFTER_SAFETY=0
HEURISTIC_HAD_REAL_ACTION_CHOICE=NO
SHADOW_HAD_REAL_CHOICE=NO
ACTION_PIPELINE_STOP_STAGE=CANDIDATE_GENERATION_RETURNED_ZERO_ACTIONS
MINIMAL_PROVIDER_STACK_CAUSAL_ROLE=NOT_CAUSAL_AS_PRIMARY;SECONDARY_FOR_PROVIDER_BACKED_ACTIONS
NO_ACTION_ROOT_CAUSE=PROVEN
```

The `DecisionTrace` class exists in code but is not serialized into V2 RAW.
Goal/blueprint/capability trace fields in enhanced telemetry are explicitly
`NOT_CAPTURED`; they must not be reconstructed from the provider inventory.

## Providers and stack

The dedicated server contains only the Director JAR in `mods/`. The bootstrap
declares these provider capabilities:

| Provider | Declared capabilities | Runtime evidence |
|---|---|---|
| `customnpcs` | `actor_source` | registered, `MISSING` |
| `gravestone` | `block_mutation`, `rollback_safe`, `structure_source` | registered, `MISSING` |
| `slenderman` | `threat_source` | not registered when mod unavailable |

The log repeatedly reports optional provider status `MISSING`. The Director
does contain provider-independent action candidates (`hint`, `recovery`), so
an external action provider is not required for every possible ACTION path.
External providers are required for provider-backed paths such as `minor`,
`ambush`, and `boss`. The current server has no required provider for those
paths, but this is secondary: the observed context never reaches their gates
because its action-driving signals and tags are empty/zero.

```
DIRECTOR_INTERNAL_ACTION_PROVIDER_EXISTS=YES
EXTERNAL_ACTION_PROVIDER_REQUIRED=YES_FOR_PROVIDER_BACKED_ACTIONS
CURRENT_SERVER_HAS_REQUIRED_ACTION_PROVIDER=NO_FOR_PROVIDER_BACKED_ACTIONS
```

## Feasibility, safety, and heuristic

For M2 and HIO, all decisions report:

```
ACTION_BEFORE_FEASIBILITY=0
ACTION_AFTER_FEASIBILITY=0
FEASIBILITY_REJECTED=0
ACTION_BEFORE_SAFETY=0
ACTION_AFTER_SAFETY=0
SAFETY_REJECTED=0
```

V4 received only `candidate:no_action` in each Shadow snapshot. It therefore
did not cause NO_ACTION and had no real action choice. Agreement with the
heuristic is descriptive only.

## Planning and execution

Current configuration has autonomous planning enabled. Execution is not
enabled (`execution.enabled=false`); autonomous execution is configured true
but is conjunctively blocked by execution being disabled. The code calls
autonomous planning after `DecisionEngine.decide()`, not before candidate
generation. Consequently:

`DOES_EXECUTION_DISABLED_PREVENT_ACTION_CANDIDATE_GENERATION=NO`

Scheduler runtime attempt/success/no-action counters are not persisted in V2,
so their numeric values are `UNKNOWN_NOT_CAPTURED`. This does not affect the
proven candidate-generation stop stage.

## What is missing for real ACTION candidates

The code-supported prerequisites are:

1. nonzero decision-driving context signals: tension/pressure above `0.25`
   for `hint`, or recovery need above `0.45` for `recovery`;
2. for provider-backed action candidates, populated provider status and
   content/location tags, including `HORROR=AVAILABLE` + `COMMON_THREAT` for
   `minor`;
3. for `ambush`/`boss`, their exact context tags and threshold gates listed
   above;
4. only after generation, valid feasibility and safety context would be
   relevant.

No implementation was made in this investigation.
