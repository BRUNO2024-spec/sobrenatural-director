# Concrete Provider Controls — Passo 3

Run scope: `phase3-20260918`. The inventory and previous runtime evidence are
authoritative. Existing proprietary archives remain read-only. `IMPLEMENTED`
means a concrete, safety-gated path exists; it does not mean autonomous use is
enabled.

## Priority and implementation plan

| MOD / PROVIDER | DOMAIN | EXISTING ADAPTER | SOURCE | CONTROL CANDIDATES | DEDICATED | CLIENT | PRIORITY | FINAL STATUS |
|---|---|---|---|---|---|---|---|---|
| CustomNPCs / `customnpcs` | entity/NPC | yes | ACTOR_SOURCE | lifecycle spawn/remove | PASS | required | HIGH | IMPLEMENTED |
| SlenderMan / `slenderman` | threat/entity | yes | THREAT_SOURCE | threat spawn/cleanup | PASS | required | HIGH | IMPLEMENTED |
| GraveStone / `gravestone` | block/structure | yes | STRUCTURE_SOURCE, BLOCK_MUTATION, ROLLBACK_SAFE | allowlisted composition/rollback | PASS_WITH_WARNINGS | required | HIGH | IMPLEMENTED |
| Twilight Forest | dimension/worldgen/entity | no | none | descriptors only | not tested | required | MEDIUM | DEFERRED_WITH_REASON |
| Welcome to the Jungle | entity/block/worldgen | no | none | descriptors only | not tested | required | MEDIUM | DEFERRED_WITH_REASON |
| Runic Dungeons | structure/worldgen/item | no | none | descriptors only | not tested | required | MEDIUM | DEFERRED_WITH_REASON |
| Mo' Villages | structure/worldgen | no | none | descriptors only | not tested | required | MEDIUM | DEFERRED_WITH_REASON |
| Baby Mobs, CreepyPastaCraft, Extra Golems | entities | no | none | source/lifecycle | not tested | required | MEDIUM | BLOCKED_MISSING_RUNTIME_PROOF |
| Grue, Herobrine, HMaG, Human Mob | threats/entities | no | none | source/lifecycle | not tested | required | MEDIUM | BLOCKED_MISSING_RUNTIME_PROOF |
| MobProperties, Souls, Special Mobs | entity/loot | no | none | source/item | not tested | required | MEDIUM | BLOCKED_MISSING_RUNTIME_PROOF |
| Resident Evil, Super Smash, Wizardry | entity/item/weapon | no | none | item/actor source | not tested | required | MEDIUM | BLOCKED_MISSING_RUNTIME_PROOF |
| Chisel, Decocraft, malisisdoors | block/item | no | none | item/block source | not tested | required | LOW | DEFERRED_WITH_REASON |
| Armourers Workshop, Cosmetic Wings | equipment/item | no | none | equipment source | not tested | required | LOW | DEFERRED_WITH_REASON |
| Just a Few Fish, SpongeBob, onepunch | entity/item | no | none | source descriptors | not tested | required | LOW | DEFERRED_WITH_REASON |
| Lumberjack, Gods Sacred Items | item/weapon/equipment | no | none | item descriptors | not tested | required | LOW | DEFERRED_WITH_REASON |
| Dynamic Lights, ambiotic | environment | no | none | environment source | not tested | required | LOW | DEFERRED_WITH_REASON |
| Place Mod, WorldEdit | block/structure/region | no | none | external mutation | not approved | required | LOW | UNSUPPORTED_EXTERNAL_MUTATION |
| MultiMine | block interaction | no | none | mining state | not tested | required | LOW | UNSUPPORTED_NOT_DIRECTOR_CONTENT |
| Morph, MobAmputation, MobDismemberment, Shatter, Mobends | entity presentation | no | none | observation only | not tested | required | LOW | UNSUPPORTED_NO_SAFE_CONTROL_API |
| Hide Names, BetterFoliage, Emotes, OptiFine | client/UI/render | no | none | none | not applicable | client | NONE | NOT_APPLICABLE |
| AsieLib, CodeChickenCore, CountryGamer-Core, CreativeCore | support/core | no | none | none | not applicable | client/server | NONE | NOT_APPLICABLE |
| DanglingVanillaFixer, iChunUtil, llibrary, malisiscore, bspkrsCore | support/core | no | none | none | not applicable | client/server | NONE | NOT_APPLICABLE |
| Coffin Mod, Carpenters resources | resource/content | no | none | audit only | not tested | required | LOW | DEFERRED_WITH_REASON |
| FNaF variants | entity/block/item | excluded | none | none | FAIL_CLIENT_CLASSLOADING | required | EXCLUDED | BLOCKED_EXTERNAL_INCOMPATIBILITY |
| ObsidianAPI | support/render | excluded | none | none | FAIL_CLIENT_CLASSLOADING | required | EXCLUDED | BLOCKED_EXTERNAL_INCOMPATIBILITY |

This table covers all 59 original archives, including support archives and the
three FNaF variants. No item, weapon, equipment, dimension, or environment
provider is advertised as concrete because no dedicated-compatible adapter and
runtime proof currently exists for those archives.

## Concrete paths closed in this phase

1. CustomNPCs existing controlled create/remove path is retained and receives
   semantic lifecycle registration only when the provider is available.
2. SlenderMan existing threat create/cleanup path is retained; natural entities
   remain observe-only and ownership markers are required for cleanup.
3. GraveStone existing composition executor remains the only world mutation
   path, with allowlist, journal, idempotency, stale-safe rollback and explicit
   dimension matching.
4. A provider-neutral `ConcreteControlRouter` validates provider availability,
   capability, dimension, authority and lease before dispatch. Adapters may
   return `UNSUPPORTED`; the router never upgrades that result.

## Explicit non-goals

No arbitrary entity attributes, movement, AI, targets, player inventory
mutation, equipment mutation, dimension travel, weather/time mutation, or
autonomous execution is enabled without provider-specific runtime proof.
