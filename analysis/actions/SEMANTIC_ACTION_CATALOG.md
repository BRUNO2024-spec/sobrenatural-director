# Semantic Action Catalog — Passo 4

The catalog is deterministic and provider-neutral. `EXECUTABLE` means that a
definition has a current provider path; it does not authorize autonomous use.

| ACTION | TYPE | FAMILY | EXECUTABLE | REQUIRED CAPABILITIES | ROLLBACK | STATUS |
|---|---|---|---:|---|---|---|
| `NO_ACTION` | primitive | RECOVERY | yes | none | none | AVAILABLE |
| `SPAWN_ACTOR` | primitive | ENTITY | yes | ACTOR_SOURCE, ENTITY_LIFECYCLE_CONTROL | compensation | AVAILABLE |
| `DESPAWN_ACTOR` | primitive | ENTITY | yes | ACTOR_SOURCE, ENTITY_LIFECYCLE_CONTROL | none | AVAILABLE |
| `SPAWN_THREAT` | primitive | THREAT | yes | THREAT_SOURCE, ENTITY_LIFECYCLE_CONTROL | compensation | AVAILABLE |
| `DESPAWN_THREAT` | primitive | THREAT | yes | THREAT_SOURCE, ENTITY_LIFECYCLE_CONTROL | none | AVAILABLE |
| `PLACE_STRUCTURE` | primitive | WORLD | yes | STRUCTURE_SOURCE, BLOCK_MUTATION | rollback required | AVAILABLE |
| `ROLLBACK_STRUCTURE` | primitive | WORLD | yes | ROLLBACK_SAFE | none | AVAILABLE |
| `PLACE_BLOCK` | primitive | WORLD | yes | BLOCK_MUTATION | rollback required | AVAILABLE_ALLOWLISTED |
| `REMOVE_BLOCK` | primitive | WORLD | no | BLOCK_MUTATION | rollback required | UNSUPPORTED_BY_CURRENT_SEMANTIC_ADAPTER |
| `REPLACE_BLOCK` | primitive | WORLD | no | BLOCK_MUTATION | rollback required | UNSUPPORTED_BY_CURRENT_SEMANTIC_ADAPTER |
| `CREATE_INVESTIGATION_SITE` | composite | INVESTIGATION | plan-only | STRUCTURE_SOURCE, ACTOR_SOURCE | compensation | AVAILABLE_IF_COMPOSED |
| `CREATE_THREAT_PRESENCE` | composite | THREAT | plan-only | THREAT_SOURCE, ENTITY_LIFECYCLE_CONTROL | compensation | AVAILABLE_IF_COMPOSED |
| `REGISTER_DISCOVERY` | primitive | DISCOVERY | no | none | none | INTERNAL_EXECUTOR_DEFERRED |
| `CREATE_HINT` | primitive | NARRATIVE | no | none | none | LEGACY_PLANNING_ONLY |
| `ENTER_RECOVERY_WINDOW` | primitive | RECOVERY | no | none | none | LEGACY_PLANNING_ONLY |
| `AMBUSH` | composite | THREAT | no | behavior, target, lifecycle | compensation | UNAVAILABLE_MISSING_CAPABILITIES |
| `BOSS_ENCOUNTER` | composite | THREAT | no | behavior, target, attributes, lifecycle | compensation | UNAVAILABLE_MISSING_CAPABILITIES |
| `MOVE_ENTITY` / `SET_BEHAVIOR` / `ASSIGN_TARGET` | primitive | ENTITY | no | unsupported controls | unknown | UNSUPPORTED |
| `GIVE_ITEM` / `EQUIP_ENTITY` | primitive | CONTENT | no | item/equipment controls | compensation | NO_APPROVED_PROVIDER |
| `DIMENSION_TRAVEL` | primitive | DIMENSION | no | travel control | none | DEFERRED |
