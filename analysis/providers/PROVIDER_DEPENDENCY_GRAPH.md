# Provider dependency graph

This graph uses archive metadata, existing runtime reports, and current adapter contracts. `NOT_DECLARED` is not treated as proof of no dependency.

## Common runtime foundation

- Every Forge 1.7.10 mod requires the Forge/Minecraft runtime matching the server.
- Director provider adapters are optional and must fail closed when the target mod is absent.

## Existing provider candidates

- `customnpcs` → `CustomNPCs_1.7.10d(19jun17).jar` → Forge/Minecraft; archive declares no required external mod; prior dedicated evidence reports PASS.
- `gravestone` → `GraveStone-2.13.0.jar` → Forge/Minecraft; prior G0 evidence reports no required external mod; client proxy/assets require a matching client installation.
- `slenderman` → `SlenderMan-3.3_1.7.10.jar` → Forge/Minecraft; archive metadata declares no dependency; prior boundary evidence reports PASS.

## Excluded dependency chains

- FNaF artifacts → FNaF upstream variants → `ObsidianAPI-0.1.1.jar` / client-side rendering surface; dedicated failure is `NoClassDefFoundError: net/minecraft/client/renderer/entity/Render` for `FNAF_Mod(v1.1).jar`.
- `ObsidianAPI-0.1.1.jar` is inventory support material, not a Director provider and not approved for public installation.

## Other original_mods

The remaining 54 archives have no existing Director adapter. Their archive-declared Forge/dependency metadata is recorded in `analysis/inventory/MODPACK_INVENTORY.csv`; they are not provider stack candidates in this phase and were not installed or boot-tested.
