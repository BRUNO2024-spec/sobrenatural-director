# Action runtime matrix

| Action path | Unit | Disposable reobf | Public plan-only | Mutation enabled |
|---|---|---|---|---|
| actor lifecycle | PASS | PASS | discovery only | NO |
| threat lifecycle | PASS | PASS | discovery only | NO |
| structure/block composition | PASS | PASS | discovery only | NO |
| investigation composite | PASS | PLAN_ONLY | PLAN_ONLY | NO |
| threat presence composite | PASS | PLAN_ONLY | PLAN_ONLY | NO |
| ambush/boss | unavailable reason tested | unavailable | unavailable | NO |

## Controlled executor closure

The disposable reobfuscated harness now runs the semantic plan through
`ActionPlanExecutor` and `ConcreteControlRouter` for CustomNPCs, SlenderMan and
GraveStone. Public runtime remains PLAN_ONLY.
