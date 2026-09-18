# Legacy candidate migration

The existing candidate IDs remain readable and are adapted to semantic action
definitions. This avoids changing historical V2 records or V4 feature inputs.

`candidate:no_action` → `NO_ACTION`.
`candidate:minor` → `CREATE_THREAT_PRESENCE` when lifecycle requirements
resolve; otherwise unavailable with the original candidate retained for
diagnostics. `candidate:ambush` and `candidate:boss` remain unavailable rather
than being falsely mapped to spawn. `candidate:hint` and `candidate:recovery`
remain planning-only until an existing factual executor is identified.
