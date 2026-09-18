# Existing provider source audit

| Provider ID | Implementation | Target mod | Bootstrap condition | Capabilities | Absent/failure behavior | Unregister / stale plan |
|---|---|---|---|---|---|---|
| `customnpcs` | `CustomNpcsProviderAdapter` | `customnpcs` | enabled and exact version `1.7.10d` | `ACTOR_SOURCE` | status missing/failed; now not registered by `registerIfAvailable`; execution preflight rejects | registry unregister increments revision; plan validation rejects unavailable provider |
| `gravestone` | `GraveStoneCapabilityProvider` | `GraveStone` | enabled and exact version `2.13.0` | `STRUCTURE_SOURCE`, `BLOCK_MUTATION`, `ROLLBACK_SAFE` | status missing/unsupported/failed; now not registered when unavailable | same registry revision and plan validation behavior |
| `slenderman` | `SlenderManThreatProvider` | `dg_slender` | enabled, supported version and startup bridge gate | `THREAT_SOURCE` | status missing/unsupported/initialization failure; now not registered when unavailable | same registry revision and threat preflight/provider lookup fail closed |

## Registry findings

- `DirectorProviderRegistry` uses `TreeMap<ProviderId,...>` and exposes
  immutable views.
- `registerIfAvailable` is the production boundary: unavailable providers do
  not enter the runtime registry.
- `unregister` removes provider and descriptor and increments the registry
  revision. Existing plan lifecycle validation uses registry availability and
  revision, so stale plans are rejected.
- `ProviderStackFingerprint` sorts provider IDs and capability IDs and can
  include externally supplied mod SHA-256 values. Missing runtime artifact
  hashes are represented as `SHA_NOT_CAPTURED`, never fabricated.

## Core isolation

Provider-specific classes remain under `provider/` and execution adapters.
Decision/candidate core consumes semantic IDs and registry descriptors rather
than external mod classes.
