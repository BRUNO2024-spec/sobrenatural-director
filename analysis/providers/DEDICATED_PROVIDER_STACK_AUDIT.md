# Dedicated provider stack audit

Run: isolated disposable Forge 1.7.10 servers under `/tmp/opencode/provider-tests`,
using a copy of the currently installed baseline Director JAR (not the
unbuilt worktree).
The public server was not modified. `original_mods/` was read-only inventory.

| Provider | Artifact / SHA-256 | Director adapter | Isolated dedicated result | Client classification | Decision |
|---|---|---|---|---|---|
| customnpcs | `CustomNPCs_1.7.10d(19jun17).jar` / `73d0036752d3931da5ed6bf5e7948185ee70c6cc46b67a2b726f24ef2b3292eb` | yes, `ACTOR_SOURCE` | Forge boot, world preparation and clean shutdown observed; Director status `AVAILABLE_SUPPORTED` | CLIENT_AND_SERVER (client GUI/model classes and entity content present) | eligible for a later client-compatible deployment test |
| gravestone | `GraveStone-2.13.0.jar` / `a3df09c5229aa4678fcc68af0277efbfe70e321a4657f91bce1ea0ef3bcf1874` | yes, `STRUCTURE_SOURCE`, `BLOCK_MUTATION`, `ROLLBACK_SAFE` policy surface | Forge boot and world preparation observed; mod logged `Data not found. Trying to load backup data` three times; baseline Director JAR reported `MISSING` (new worktree adapter not tested) | CLIENT_AND_SERVER pending client registry verification | blocked pending new-build adapter/runtime validation |
| dg_slender | `SlenderMan-3.3_1.7.10.jar` / `f2a8a4592130b36cfd8cc4c322d7eea23dfbdf2a868c2d7b7db2b1de3427eab7` | yes, threat adapter | Forge boot, world preparation and shrine generation observed; baseline Director JAR reported `MISSING` (new worktree adapter not tested) | CLIENT_AND_SERVER (entity/spawn content) | blocked pending new-build adapter/runtime validation |
| FNaF / Obsidian | inventory only | no approved dedicated deployment | excluded by the existing dedicated client/LWJGL blocker | CLIENT_AND_SERVER | excluded |

## Current public stack

The public server contains only `SobrenaturalDirector-0.12.0-alpha.jar`.
No provider JAR was installed by this phase. Provider-backed field gameplay is
therefore not claimed.

## Evidence boundary

The isolated tests prove boot/world-load behavior only for the baseline Director
JAR. They do not prove
provider-backed action execution, client handshake compatibility, or safe
content mutation. Those remain mandatory gates before deployment.
