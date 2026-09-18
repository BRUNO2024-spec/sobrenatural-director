# DecisionContext live-source audit

## Current production path

`DirectorRuntimeCoordinator.onServerTick` derives `PlayerModel` and
`WorldModel`, then calls `DecisionContextAssembler.assemblePerPlayer`. The
result is consumed immediately by `DecisionEngine`; autonomous narrative
planning is invoked afterwards and is not the source of the observed candidate
set.

## Field map

| Field | Current source/behavior | Canonical available source | Plan |
|---|---|---|---|
| `tension` | literal `0` in `DecisionContextAssembler` | no numeric canonical field; `AdaptivePacingDirector` exposes escalation allowance and mode | adapt pacing allowance with explicit provenance; unavailable remains explicit |
| `pressure` | literal `0` | `AdaptivePacingDirector` pacing/recovery state; persisted pacing history | use pacing-derived pressure adapter, never silent default |
| `recoveryNeed` | literal `0` | `PacingAssessment.recoveryNeed` | pass the existing pacing assessment into assembly |
| `eventConcurrency` | literal `0` | active narrative plans/threads/events in `DirectorWorldState` | count active lifecycle records |
| `providers` | empty map | `DirectorProviderRegistry` | project registered provider statuses and capabilities |
| `contentTags` | empty set | semantic catalog/runtime provider capability state | expose only runtime-proven semantic tags |
| `locationTags` | empty set | `SemanticRegionProfile`, environment evidence, player environment snapshot | adapt existing region classification/signals |
| `underground` | literal `false` | player `EnvironmentSnapshot` has `canSeeSky` and `y` | carry existing player environment signal into the derived model |
| `safetyKnown` | literal `false` | environment/profile safety policy inputs | retain conservative false until a real safety assessment exists |

## Important constraints

- No artificial tension/pressure is introduced.
- No tag is added merely because a JAR exists.
- Missing sources are represented as `UNAVAILABLE` provenance and counted as
  explicit fallbacks.
- The V4 scorer remains observation-only and cannot affect decisions or
  execution.

The provider JARs in `original_mods/` are inventory only at this stage. The
public server currently contains only the Director JAR, and no provider is
installed or enabled by this audit.
