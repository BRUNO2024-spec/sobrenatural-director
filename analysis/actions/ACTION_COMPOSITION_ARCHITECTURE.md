# Action composition architecture

`ActionPlan` is a bounded deterministic DAG. Nodes have stable IDs, action
type, dependencies, optionality, requirements, estimated costs, rollback
policy and failure policy. The composer validates cycles before returning a
plan and never performs world access.

Composite plans use compensation in reverse dependency order when a required
step fails. A plan is `COMPLETED` only when all required nodes succeed;
`PARTIAL`, `COMPENSATED`, `ABORTED`, `SAFETY_REJECTED` and `FAILED` remain
distinct outcomes.

The first honest composites are `CREATE_INVESTIGATION_SITE` (structure step
plus optional actor step) and `CREATE_THREAT_PRESENCE` (threat lifecycle step).
Neither is automatically scheduled or executed in the public server.
