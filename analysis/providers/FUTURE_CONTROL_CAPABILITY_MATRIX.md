# Future control capability matrix

`DEFERRED` means a semantic candidate only; it is not a runtime provider
claim. Existing adapters are shown separately from inventory possibilities.

| Mod/content family | Source domains | Candidate controls | Adapter | Priority |
|---|---|---|---|---|
| CustomNPCs | actors, NPCs | attributes, movement, behavior, target, lifecycle, equipment | yes (source only) | high |
| SlenderMan | threats, entities | lifecycle, target, behavior | yes (source only) | high |
| GraveStone | blocks, structures, loot | block mutation, composition, rollback | yes (bounded) | high |
| Twilight Forest | entities, structures, dimensions, worldgen | scoped source, composition | no | medium |
| Welcome to the Jungle | mobs, blocks, items, worldgen | source descriptors | no | medium |
| Runic Dungeons / Mo' Villages | structures, worldgen | structure source/composition | no | medium |
| Wizardry / HMaG / Super Smash | entities, items, weapons | actor/item source | no | medium |
| remaining identified mods | domain-specific | evidence-driven source adapters | no | low |
| 13 unresolved support/client archives | unknown | none until identified | no | none |

Weapon remains an item semantic tag; no redundant `WEAPON` resource domain is
introduced.
