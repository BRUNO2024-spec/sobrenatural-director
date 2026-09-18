# REAL_PROVIDER_STACK phase 1 plan

## Scope

Discovery, static metadata, provider/adaptor audit, dependency and
client/server classification, isolated dedicated validation, registry closure,
fingerprints and manifests. No new gameplay behavior, entity control, ML,
Learned Control, Advisory Mode or autonomous execution enablement.

## Authoritative inputs

- `original_mods/` is read-only inventory input.
- `analysis/inventory/MODPACK_INVENTORY.csv` is the generated archive inventory.
- Current Director source under `director/src/main/java` is authoritative for
  adapters and registration behavior.
- Existing disposable-runtime evidence is used only where its artifact SHA
  matches the current inventory.

## Current provider boundary

Existing adapters only:

| Provider | Implementation | Registration | Capabilities |
|---|---|---|---|
| `customnpcs` | `CustomNpcsProviderAdapter` | `ProviderBootstrap.registerAll` → availability-gated registry | `ACTOR_SOURCE` |
| `gravestone` | `GraveStoneCapabilityProvider` | same | `STRUCTURE_SOURCE`, `BLOCK_MUTATION`, `ROLLBACK_SAFE` policy surface |
| `slenderman` | `SlenderManThreatProvider` | same | `THREAT_SOURCE` |

Unavailable providers are not registered. This is fail-closed and prevents an
archive's mere presence from becoming runtime availability.

## Ordered gates

1. Inventory all 59 original archives and preserve hashes.
2. Build the mod/provider matrix and dependency graph.
3. Reuse existing dedicated evidence only for matching artifacts.
4. Validate each provider in a disposable Forge server, one mod at a time,
   using a reobfuscated Director artifact.
5. Run provider contract, unregister, stale-plan and deterministic fingerprint
   tests.
6. Validate `ProviderRegistry → DecisionContextAssembler → DecisionContext`.
7. Produce client/server manifests and machine-readable prebuild/closure gates.
8. Keep the public server stopped and autonomous execution disabled until all
   phase gates are complete.

## Explicit non-goals

No provider not already implemented in Director will be added in this phase.
No proprietary binary, world, log, RAW data or credential is committed.
