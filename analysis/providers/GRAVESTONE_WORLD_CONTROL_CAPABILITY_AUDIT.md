# GraveStone world control capability audit

Phase 1 runtime evidence confirms GraveStone `2.13.0` registry content,
structures, blocks/items, and the existing rollback-safe provider boundary.
Fresh-world backup-data warnings are upstream non-fatal warnings.

| Capability | Status |
|---|---|
| structure/block source | SUPPORTED |
| allowlisted mutation planning and rollback journal | SUPPORTED by existing foundation |
| allowlisted structure composition and block mutation | SUPPORTED | concrete executor, journal and rollback path |
| player-area mutation, item inventory control | UNSUPPORTED_IN_PHASE3 | ownership policy rejects it |
| stale-safe restoration | SUPPORTED | dimension and observed-state checks reject stale rollback |
