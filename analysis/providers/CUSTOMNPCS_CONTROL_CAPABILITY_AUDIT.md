# CustomNPCs control capability audit

Archive evidence confirms actor/NPC entities, controllers, AI, items, blocks,
and scripting packages. Phase 1 runtime evidence confirms provider discovery;
it does not prove mutation APIs.

| Capability | Status | Boundary |
|---|---|---|
| actor/entity source | SUPPORTED | existing provider contract |
| spawn/despawn lifecycle | SUPPORTED | version-pinned bridge, Director-owned journal and semantic router |
| attributes, movement, target, AI/role, faction, equipment | UNSUPPORTED_IN_PHASE3 | no safe public API/runtime proof; not advertised |
| restore state | SUPPORTED_FOR_LIFECYCLE | cleanup/compensation path; attribute restore not claimed |

No claim here enables control or changes the proprietary archive.
