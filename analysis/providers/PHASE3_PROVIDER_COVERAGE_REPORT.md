# Passo 3 provider coverage report

This is the final status of every identified content archive from the Passo 2
inventory. `DEFERRED` is an explicit result of the audit, not an omitted
provider. The public stack remains limited to the three approved providers.

## Implemented concrete providers

| Mod | Provider | Concrete controls | Runtime status |
|---|---|---|---|
| CustomNPCs | `customnpcs` | Director-owned NPC lifecycle create/remove; journaled cleanup | IMPLEMENTED |
| SlenderMan | `slenderman` | Director-owned threat lifecycle spawn/cleanup; threat journal | IMPLEMENTED |
| GraveStone | `gravestone` | allowlisted block/structure composition, journal, rollback, stale rejection | IMPLEMENTED |

## Identified archives with explicit non-provider status

| Mod | Domains | Status | Reason |
|---|---|---|---|
| Armourer's Workshop | item/equipment | DEFERRED | no dedicated-compatible adapter/runtime proof |
| Baby Mobs | entity/mob | BLOCKED | no runtime proof |
| Chisel | block/item | DEFERRED | no adapter |
| Coffin Mod | block/item | DEFERRED | no adapter |
| Cosmetic Wings | item/equipment | DEFERRED | no adapter |
| CreepyPastaCraft | entity/item | BLOCKED | no runtime proof |
| Decocraft | block/item/equipment | DEFERRED | no adapter |
| Dynamic Lights | environment | DEFERRED | client/core behavior, no Director control API |
| Electroblob's Wizardry | entity/item/weapon/block | DEFERRED | no adapter |
| Extra Golems | entity/mob | BLOCKED | no runtime proof |
| FNAF Mod, FNaF Universe, FNAF legacy ZIP | entity/block/item | BLOCKED | dedicated client classloading incompatibility |
| Gods Sacred Items | item/weapon/equipment | DEFERRED | no adapter |
| Grue | threat/entity | BLOCKED | no runtime proof |
| Herobrine | threat/entity | BLOCKED | no runtime proof |
| Hide Names | client/UI | NOT_APPLICABLE | no Director content control |
| Hostile Mobs and Girls | entity/item/block | BLOCKED | no runtime proof |
| Human Mob Mod | entity/NPC | BLOCKED | no runtime proof |
| Just a Few Fish | entity/item/block | DEFERRED | no adapter |
| Lumberjack | item/weapon | DEFERRED | no adapter |
| Mo' Villages | structure/worldgen | BLOCKED | no runtime proof |
| Mob Amputation, Mob Dismemberment | entity presentation | UNSUPPORTED | no safe Director control API |
| Mob Properties | entity/loot | BLOCKED | no runtime proof |
| Morph | entity presentation | UNSUPPORTED | transformation ownership unsafe |
| MultiMine | block interaction | UNSUPPORTED | not Director-owned content |
| Place Mod | block/structure | UNSUPPORTED | external mutation surface not approved |
| Runic Dungeons | structure/worldgen/item | BLOCKED | no runtime proof |
| Shatter | entity presentation | UNSUPPORTED | no safe Director control API |
| Souls | entity/item/loot | BLOCKED | no runtime proof |
| Special Mobs | entity/mob | BLOCKED | no runtime proof |
| SpongeBob SquarePants | entity/item | DEFERRED | no adapter |
| Super Smash Bros | entity/item/weapon | DEFERRED | no adapter |
| Welcome to the Jungle | entity/block/item/worldgen | BLOCKED | no runtime proof |
| ambiotic | environment | DEFERRED | no adapter |
| malisisdoors | block/item/structure | DEFERRED | no adapter and dependency closure absent |
| onepunch | entity/item | DEFERRED | no adapter |
| Resident Evil | entity/item/block/worldgen | BLOCKED | no runtime proof; excluded addon chain |
| Twilight Forest | entity/structure/dimension/worldgen | BLOCKED | no runtime proof and client dependency not approved |
| WorldEdit | block/structure/region | UNSUPPORTED | external tool mutation boundary |
| Carpenters cached resources | resource archive | NOT_APPLICABLE | no standalone runtime provider |
| ObsidianAPI | render/support | BLOCKED | excluded dependency/classloading chain |

## Support archives (not content providers)

`AsieLib`, `BetterFoliage`, `CodeChickenCore`, `CountryGamer-Core`,
`CreativeCore`, `DanglingVanillaFixer`, `Emotes`, `iChunUtil`, `llibrary`,
`malisiscore`, `Mobends`, `OptiFine`, and `bspkrsCore` remain explicitly
`NOT_APPLICABLE` or `DEFERRED_SUPPORT_ONLY`. They were not installed as new
providers and no capability is advertised for them.

## Domain conclusion

Concrete entity/world controls are available for the approved stack. No safe
item/weapon/equipment or dimension-control adapter was proven by the real
inventory and approved dedicated runtime, so those domains remain fail-closed
and are not represented as supported capabilities.
