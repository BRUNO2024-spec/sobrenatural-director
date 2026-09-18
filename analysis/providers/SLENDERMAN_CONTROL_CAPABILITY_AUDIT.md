# SlenderMan control capability audit

The archive contains `net.dudgames.slender.entity` and a dedicated mob
spawner. Phase 1 runtime evidence confirms threat discovery/provider
availability. Spawn/control/AI/target/attribute and restoration behavior are
not promoted beyond the existing source contract.

| Capability | Status |
|---|---|
| threat/entity source | SUPPORTED |
| spawn API | REQUIRES_RUNTIME_PROOF |
| attributes, AI, target behavior, movement, lifecycle, restore | UNKNOWN |
| arbitrary player control | UNSUPPORTED by policy |
