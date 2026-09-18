# World / Content Control Foundation — Passo 2

## Boundary

This phase adds provider-neutral contracts for observation and future control.
It does not add concrete control adapters, does not change V4 feature schema,
and does not authorize execution. `customnpcs`, `gravestone`, and `slenderman`
remain the only runtime providers.

## Capability layers

Existing source capabilities remain compatible:

| Layer | Capabilities | Decision |
|---|---|---|
| Source | `ACTOR_SOURCE`, `THREAT_SOURCE`, `STRUCTURE_SOURCE` | preserve existing semantics |
| Source | `BLOCK_SOURCE`, `ITEM_SOURCE`, `DIMENSION_SOURCE`, `ENVIRONMENT_SOURCE` | foundation descriptors only; deferred concrete providers |
| Control | `ENTITY_ATTRIBUTE_CONTROL`, `ENTITY_MOVEMENT_CONTROL`, `ENTITY_BEHAVIOR_CONTROL`, `ENTITY_TARGET_CONTROL`, `ENTITY_LIFECYCLE_CONTROL` | contracts only |
| Control | `BLOCK_MUTATION`, `STRUCTURE_COMPOSITION`, `ITEM_GIVE`, `ITEM_DROP`, `ITEM_REMOVE`, `EQUIPMENT_CONTROL`, `ENVIRONMENT_MUTATION` | contracts only; existing block mutation safety is retained |
| Control | `DIMENSION_ACTION` | context/availability only; travel control intentionally absent |

Capabilities describe a semantic operation, never a mod name. A provider may
advertise only a capability whose contract test and runtime evidence exist.
Unsupported and unavailable states fail closed.

## Resource model

`DimensionRef` is mandatory in `WorldPositionRef`; equal x/y/z values in two
dimensions are different targets. References contain stable bounded values and
never `World`, `WorldServer`, `Entity`, `EntityPlayer`, `ItemStack`, or a
provider runtime object. They are therefore safe to pass to planners and
async workers.

## Ownership and safety

Entity ownership distinguishes natural/manual/unknown content from
Director-spawned or adopted content. Block, item, and structure provenance is
explicit. `UNKNOWN` and player-owned content are conservative: observation is
allowed, mutation is rejected unless a later explicit policy proves authority.

Every future mutation follows availability → capability → reference/dimension
validity → ownership/authority → safety/budget preflight. Requests are not
executions. Results use typed terminal statuses rather than exceptions as
normal control flow.

## Lease and restoration

`ControlLease` is immutable and bounded, with source, target, expiry, and a
restoration policy. Temporary changes require `RESTORABLE`; `UNKNOWN` is not
accepted where restoration is required. No live mutable state is persisted.

## Catalog and registry integration

The existing deterministic semantic catalog is the catalog boundary. Provider
descriptors and catalog entries are sorted by stable identifiers, and provider
unregistration invalidates their contributions by registry revision. A future
runtime catalog may add dimension/location filters without serializing the
full catalog into `DecisionContext`; only bounded availability summaries are
appropriate there.

## Explicit deferrals

- no entity spawn, AI, movement, target, item, equipment, weather, time, or
  cross-dimension mutation;
- no teleport/travel capability;
- no automatic provenance inference for historical player blocks/items;
- no new real-world gameplay collection;
- no change to Autonomous Execution, Learned Control, Advisory Mode, or V4.

## Recommended Passo 3 order

Prioritize concrete adapters only after client readiness and disposable runtime
fixtures are available. The inventory indicates the greatest immediate value
in actor/threat observation (`CustomNPCs`, `SlenderMan`), followed by the
already validated GraveStone block/structure boundary. Other content adapters
remain evidence-driven and must not be selected solely by mod name.
