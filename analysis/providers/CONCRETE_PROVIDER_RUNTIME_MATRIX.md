# Concrete provider runtime matrix

| PROVIDER | CAPABILITY | UNIT | INTEGRATION | REOBF | DEDICATED | RESTORATION | FAIL_CLOSED | DIMENSION_SAFE | OWNERSHIP_SAFE | STATUS |
|---|---|---|---|---|---|---|---|---|---|---|
| customnpcs | ACTOR_SOURCE | PASS | PASS | PASS | PASS | n/a | PASS | PASS after fix | PASS | IMPLEMENTED |
| customnpcs | ENTITY_LIFECYCLE_CONTROL | PASS | PASS | PASS | PASS | cleanup journal | PASS | PASS after fix | PASS | IMPLEMENTED |
| slenderman | THREAT_SOURCE | PASS | PASS | PASS | PASS | cleanup journal | PASS | PASS after fix | PASS | IMPLEMENTED |
| slenderman | ENTITY_LIFECYCLE_CONTROL | PASS | PASS | PASS | PASS | cleanup journal | PASS | PASS after fix | PASS | IMPLEMENTED |
| gravestone | STRUCTURE_SOURCE | PASS | PASS | PASS | PASS | n/a | PASS | PASS after fix | PASS | IMPLEMENTED |
| gravestone | BLOCK_MUTATION | PASS | PASS | PASS | PASS | journal | PASS | PASS after fix | PASS | IMPLEMENTED |
| gravestone | ROLLBACK_SAFE | PASS | PASS | PASS | PASS | stale-safe | PASS | PASS after fix | PASS | IMPLEMENTED |
| optional item providers | ITEM_SOURCE / weapon tags | n/a | n/a | n/a | n/a | n/a | PASS | n/a | n/a | DEFERRED_NO_APPROVED_ADAPTER |
| optional dimension providers | DIMENSION_SOURCE | n/a | n/a | n/a | n/a | n/a | PASS | PASS | n/a | DEFERRED_NO_APPROVED_ADAPTER |
