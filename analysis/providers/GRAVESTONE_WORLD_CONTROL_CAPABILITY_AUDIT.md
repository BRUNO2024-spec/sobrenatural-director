# GraveStone world control capability audit

Phase 1 runtime evidence confirms GraveStone `2.13.0` registry content,
structures, blocks/items, and the existing rollback-safe provider boundary.
Fresh-world backup-data warnings are upstream non-fatal warnings.

| Capability | Status |
|---|---|
| structure/block source | SUPPORTED |
| allowlisted mutation planning and rollback journal | SUPPORTED by existing foundation |
| arbitrary structure composition, player-area mutation, item inventory control | DEFERRED_TO_PHASE3 |
| stale-safe restoration | FOUNDATION_ONLY; concrete proof deferred |
