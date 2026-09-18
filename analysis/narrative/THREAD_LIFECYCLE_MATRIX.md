# Thread lifecycle matrix

| Thread state | Situation relationship | Restart behavior | Terminal behavior |
|---|---|---|---|
| ACTIVE | new opaque situation IDs may be linked | restore membership | remains active until factual resolution |
| SUSPENDED | no replay of completed situations | restore suspended state | explicit resume only |
| DORMANT | continuation requires existing policy | restore dormant state | no implicit revival |
| RESOLVED | no new membership | restore terminal state | reject transition to active |
| ABANDONED | no new membership | restore terminal state | reject transition to active |
