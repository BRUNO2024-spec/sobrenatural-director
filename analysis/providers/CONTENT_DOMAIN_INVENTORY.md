# Content domain inventory — Passo 2

Status: **static audit complete**. The authoritative input is
`analysis/inventory/MODPACK_INVENTORY.csv`; archives under `original_mods/` were
not modified or executed. A domain is marked only when the archive metadata,
package names, class inventory, or the previously recorded runtime evidence
supports it. A blank/unknown role is not treated as absence of content.

## Classification rules

- `CONFIRMED_STATIC`: package/class/registry evidence is present in the archive.
- `RUNTIME_CONFIRMED`: the Phase 1 disposable Forge evidence also observed it.
- `UNKNOWN`: identity or content role could not be established safely from the
  available archive evidence. These entries remain deferred and are not
  installed by this phase.
- FNaF and Obsidian remain **excluded** by the existing dedicated client
  classloading incompatibility; their static domains do not make them providers.

`WEAPONS` is reported separately for audit purposes, but the architecture
models weapons as semantic item tags rather than a parallel resource domain.

## Identified archives (46)

| Archive / mod id | Evidence-backed domains | Evidence status | Adapter / disposition |
|---|---|---|---|
| Armourer's Workshop / `armourersWorkshop` | ITEMS, EQUIPMENT | CONFIRMED_STATIC | deferred |
| Baby Mobs / `babymobs` | ENTITIES, MOBS | CONFIRMED_STATIC | deferred |
| Chisel / `chisel` | BLOCKS, ITEMS | CONFIRMED_STATIC | deferred |
| Coffin Mod / `examplemod` | BLOCKS, ITEMS | CONFIRMED_STATIC | deferred |
| Cosmetic Wings / `cosmeticWings` | ITEMS, EQUIPMENT | CONFIRMED_STATIC | deferred |
| CreepyPastaCraft / `cpc` | ENTITIES, MOBS, ITEMS | CONFIRMED_STATIC | deferred |
| CustomNPCs / `customnpcs` | ENTITIES, NPCS, ACTORS | RUNTIME_CONFIRMED | existing `ACTOR_SOURCE` |
| Decocraft / `props` | BLOCKS, ITEMS, EQUIPMENT | CONFIRMED_STATIC | deferred |
| Dynamic Lights / `DynamicLights*` | ENVIRONMENT | CONFIRMED_STATIC | deferred |
| Electroblob's Wizardry / `wizardry` | ENTITIES, ITEMS, WEAPONS, BLOCKS | CONFIRMED_STATIC | deferred |
| Extra Golems / `golems` | ENTITIES, MOBS | CONFIRMED_STATIC | deferred |
| FNAF Mod / `FNAM` | ENTITIES, BLOCKS, ITEMS | CONFIRMED_STATIC | excluded; client incompatibility |
| FNaF Universe / `fnafmod` | ENTITIES, BLOCKS, ITEMS | CONFIRMED_STATIC | excluded; client incompatibility |
| FNAF legacy ZIP / `FNAFMod` | UNKNOWN | UNKNOWN | excluded; metadata is incomplete |
| Gods Sacred Items / `heart` | ITEMS, WEAPONS, EQUIPMENT, LOOT | CONFIRMED_STATIC | deferred |
| GraveStone / `GraveStone` | BLOCKS, STRUCTURES, ITEMS, LOOT | RUNTIME_CONFIRMED | existing source/mutation provider |
| Grue / `grue` | ENTITIES, THREATS | CONFIRMED_STATIC | deferred |
| Herobrine / `herobrinemod` | ENTITIES, THREATS | CONFIRMED_STATIC | deferred |
| Hide Names / `HideNames` | OTHER | CONFIRMED_STATIC | client/UI utility; deferred |
| Hostile Mobs and Girls / `HMaG` | ENTITIES, MOBS, ITEMS, WEAPONS, BLOCKS | CONFIRMED_STATIC | deferred |
| Human Mob Mod / `Humans` | ENTITIES, NPCS | CONFIRMED_STATIC | deferred |
| Just a Few Fish / `jaff` | ENTITIES, MOBS, ITEMS, BLOCKS | CONFIRMED_STATIC | deferred |
| Lumberjack / `Lumberjack` | ITEMS, WEAPONS | CONFIRMED_STATIC | deferred |
| Mo' Villages / `movillages` | STRUCTURES, WORLDGEN, ENVIRONMENT | CONFIRMED_STATIC | deferred |
| Mob Amputation / `MobAmputation` | ENTITIES, MOBS | CONFIRMED_STATIC | deferred |
| Mob Dismemberment / `MobDismemberment` | ENTITIES, MOBS | CONFIRMED_STATIC | deferred |
| Mob Properties / `MobProperties` | ENTITIES, MOBS, ITEMS, LOOT | CONFIRMED_STATIC | deferred |
| Morph / `Morph` | ENTITIES, ACTORS | CONFIRMED_STATIC | deferred |
| MultiMine / `AS_MultiMine` | BLOCKS | CONFIRMED_STATIC | transformer/utility; deferred |
| Place Mod / nested archive | BLOCKS, STRUCTURES | CONFIRMED_STATIC | deferred; nested archive not installed |
| Runic Dungeons / `runicdungeons` | BLOCKS, ITEMS, STRUCTURES, WORLDGEN | CONFIRMED_STATIC | deferred |
| Shatter / `shatter` | ENTITIES, MOBS | CONFIRMED_STATIC | deferred |
| SlenderMan / `dg_slender` | ENTITIES, THREATS | RUNTIME_CONFIRMED | existing `THREAT_SOURCE` |
| Souls / `Souls` | ENTITIES, ITEMS, LOOT | CONFIRMED_STATIC | deferred |
| Special Mobs / `SpecialMobs` | ENTITIES, MOBS | CONFIRMED_STATIC | deferred |
| SpongeBob SquarePants / `SpongeBobSquarePantsMod` | ENTITIES, ITEMS | CONFIRMED_STATIC | deferred |
| Super Smash Bros / `jawser` | ENTITIES, ITEMS, WEAPONS | CONFIRMED_STATIC | deferred |
| Welcome to the Jungle / `thejungle` | ENTITIES, MOBS, BLOCKS, ITEMS, WORLDGEN, ENVIRONMENT | CONFIRMED_STATIC | deferred |
| ambiotic / `ambiotic` | ENVIRONMENT | CONFIRMED_STATIC | deferred |
| malisisdoors / `malisisdoors` | BLOCKS, ITEMS, STRUCTURES | CONFIRMED_STATIC | deferred |
| onepunch / `onepunch` | ENTITIES, ITEMS | CONFIRMED_STATIC | deferred |
| Resident Evil / `residentevil` | ENTITIES, MOBS, ITEMS, EQUIPMENT, BLOCKS, WORLDGEN | CONFIRMED_STATIC | deferred |
| Twilight Forest / `TwilightForest` | ENTITIES, MOBS, BLOCKS, ITEMS, STRUCTURES, WORLDGEN, DIMENSIONS | CONFIRMED_STATIC | deferred |
| WorldEdit / `WorldEdit` | BLOCKS, STRUCTURES, REGIONS | CONFIRMED_STATIC | deferred; external tool boundary |
| Carpenters cached resources | BLOCKS | CONFIRMED_STATIC | resource archive only |
| Obsidian API / `obsidianAPI` | OTHER | CONFIRMED_STATIC | excluded; client incompatibility |

The last three rows are retained as identified archive roles even though they
are not provider candidates; this keeps the inventory domain-complete and
prevents dependency/resource archives from being silently discarded.

## Unresolved archives (13)

The following archives are present in the 59-archive inventory but are not
counted as identified content providers: `AsieLib`, `BetterFoliage`,
`CodeChickenCore`, `CountryGamer-Core`, `CreativeCore`, `DanglingVanillaFixer`,
`Emotes`, `iChunUtil`, `llibrary`, `malisiscore`, `Mobends`, `OptiFine`, and
`[1.7.10]bspkrsCore`. They are support, transformer, client, or incomplete
metadata archives. Their presence is recorded; no domain capability is
claimed until a safe archive-level audit can establish one.

## Coverage summary

Counts are counts of identified archives, not individual registry entries. A
mod can contribute to multiple columns.

```text
IDENTIFIED_MOD_COUNT=46
UNKNOWN_MOD_COUNT=13
MODS_WITH_ENTITIES=26
MODS_WITH_BLOCKS=18
MODS_WITH_STRUCTURES=7
MODS_WITH_ITEMS=24
MODS_WITH_WEAPONS=5
MODS_WITH_EQUIPMENT=5
MODS_WITH_DIMENSIONS=1
MODS_WITH_WORLDGEN=5
MODS_WITH_ENVIRONMENT_CONTENT=4
```

These are planning counts. They do not assert that a provider currently
implements any deferred control. Provider registration remains limited to the
three Phase 1 providers.
