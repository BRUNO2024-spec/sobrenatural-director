# Autonomous action policy matrix

| Action type | Candidate source | Provider required | Executor foundation | Safety / rollback | Autonomous now |
|---|---|---|---|---|---|
| `NO_ACTION` | candidate generator | no | n/a | n/a | not an action |
| `HINT` | live tension gate | no | no verified production executor in current coordinator | non-destructive, but executor path not proven | NO |
| `RECOVERY` | live recovery gate | no | planning path exists; end-to-end executor not proven | non-destructive, lifecycle not proven | NO |
| provider-backed actor | provider registry | CustomNPCs | adapter and preparer exist | provider/client/fresh-plan/safety preflight required | NO |
| provider-backed structure | provider registry | GraveStone | preparer and rollback policy exist | allowlist, journal, stale-state rollback required | NO |
| threat | provider registry | Slender | preparer exists | provider validity and player safety required | NO |
| ambush / boss / block mutation | capability and normal gates | yes | high-risk execution not field validated | hard safety and rollback required | NO |

Autonomous execution remains disabled until the complete action E2E and safety
gate passes. V4 remains Shadow-only and cannot influence decision, safety, or
execution.
