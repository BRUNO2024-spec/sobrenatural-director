# SlenderMan control capability audit

The archive contains `net.dudgames.slender.entity` and a dedicated mob
spawner. Phase 1 runtime evidence confirms threat discovery/provider
availability. Spawn/control/AI/target/attribute and restoration behavior are
not promoted beyond the existing source contract.

| Capability | Status |
|---|---|
| threat/entity source | SUPPORTED |
| spawn/cleanup lifecycle | SUPPORTED | existing threat journal, ownership marker and semantic router |
| attributes, AI, target behavior, movement | UNSUPPORTED_IN_PHASE3 | no safe public API/runtime proof; not advertised |
| restoration | SUPPORTED_FOR_LIFECYCLE | cleanup of Director-owned threat only |
| arbitrary player control | UNSUPPORTED by policy |
