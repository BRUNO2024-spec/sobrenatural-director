# ActionPlan executor gap audit

## Before this closure

| Definition | Existing concrete path | Binding before closure | Result |
|---|---|---|---|
| `NO_ACTION` | no mutation | none required | terminal no-op |
| `SPAWN_ACTOR`, `DESPAWN_ACTOR` | `EntityControlRequest` → `ConcreteControlRouter` → CustomNPCs adapter | absent | gap |
| `SPAWN_THREAT`, `DESPAWN_THREAT` | `EntityControlRequest` → router → SlenderMan adapter | absent | gap |
| `PLACE_STRUCTURE`, `ROLLBACK_STRUCTURE` | `ControlledCompositionExecutor` and GraveStone journal | not a `ControlRequest` | gap |

The catalog's executable count therefore overstated the runtime closure: it
described provider paths but had no plan-level request factory or executor.

## Closure design

`ActionExecutionBindingRegistry` is a deterministic table from `ActionType` to
a provider-neutral binding. Entity bindings require both source and lifecycle
capabilities and construct only existing `EntityControlRequest` objects.
Structure bindings construct `StructureControlRequest`, whose GraveStone
adapter delegates to the existing allowlisted `ControlledCompositionExecutor`.
The core executor has no provider/mod-specific branch.

`ActionPlanExecutor` validates the plan at execution time, checks the target
dimension against the server world, resolves the binding/provider immediately
before each step, routes through `ConcreteControlRouter`, records a plan-level
journal entry, and compensates applied steps in reverse topological order.
Provider journals remain authoritative; the plan journal stores references and
factual control statuses rather than duplicating mutations.

## Runtime closure

Execution is explicit and requires a server-thread `ControlExecutionContext`.
The disposable reobfuscated harness verified actor and threat lifecycle,
structure apply/rollback, composite compensation, provider unregister,
missing-content and dimension stale rejection. The public server remains
plan-only because its execution configuration is disabled.
