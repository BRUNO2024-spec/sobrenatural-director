# Action pipeline convergence audit — Passo 4

## Current pipelines

### Narrative / situation path

`SituationContext` → `SituationEvaluator` → `SituationGoalCandidate` →
`SituationBlueprintComposer` → `BlueprintCandidate` → `RoleCapabilityResolver`
→ `SituationIntelligencePipeline` → `CapabilityPlanner` → `CandidatePlan`.

This path is provider-neutral and already carries dimension, goal, blueprint
roles, pacing and memory. Its terminal object is a capability selection, not a
semantic runtime action. `GoalRequirementResolver` is coarser than blueprint
requirements and currently does not represent control lifecycle requirements.

### Runtime decision path

`DecisionContextAssembler` → `DecisionEngine` → `CandidateGenerator` →
`StandardConstraints` → `UtilityEvaluator` → selected `CandidateAction`.

The generator currently emits string identities such as `candidate:no_action`,
`candidate:minor`, `candidate:ambush`, `candidate:boss`, `candidate:hint` and
`candidate:recovery`. Provider requirements are free-form maps and the engine
does not resolve them through `ProviderRegistry` or `SemanticCatalog`.

## Convergence decision

`SemanticActionCatalog` is the single shared definition layer. It is not a
provider registry and it is not an executor.

- Situation/Goal/Blueprint produces an `ActionIntent`/action type request.
- The catalog resolves definition, requirements, availability and reason.
- The same definition is adapted to a legacy `CandidateAction` for the
  existing `DecisionEngine`, preserving V4 feature schema and old consumers.
- `ActionComposer` produces a deterministic action plan/DAG from the same
  definitions; it does not call a provider.
- Existing concrete execution bridges remain the only low-level execution
  boundary and are reached only by later controlled harnesses.

## Legacy mappings

| Legacy candidate | Semantic mapping | Current status |
|---|---|---|
| `candidate:no_action` | `NO_ACTION` | executable, non-mutating |
| `candidate:minor` | `CREATE_THREAT_PRESENCE` / threat lifecycle | executable when `THREAT_SOURCE` and lifecycle control resolve |
| `candidate:ambush` | no honest direct mapping | unavailable; behavior/target controls are absent |
| `candidate:boss` | no honest direct mapping | unavailable; orchestration/target controls are absent |
| `candidate:hint` | `CREATE_HINT` | planning-only until an existing hint executor is identified |
| `candidate:recovery` | `ENTER_RECOVERY_WINDOW` | planning-only/internal; no provider mutation |

`AMBUSH` and `BOSS` are not renamed spawn actions. Their missing semantic
requirements remain visible as unavailable reasons.

## Responsibility boundaries

| Concern | Owner |
|---|---|
| Goal/blueprint narrative meaning | `situation` package |
| Definition and availability | `action` package/catalog |
| Provider and content resolution | existing `ProviderRegistry`/semantic catalog |
| Candidate scoring | existing `DecisionEngine` and utility layer |
| DAG validation/composition | `ActionComposer`/`ActionPlanValidator` |
| Concrete mutation | existing provider control bridges |
| factual outcome | execution/journal layer; never the shadow model |

No second runtime action catalog is introduced. Autonomous Execution remains
off and public validation is plan-only.
