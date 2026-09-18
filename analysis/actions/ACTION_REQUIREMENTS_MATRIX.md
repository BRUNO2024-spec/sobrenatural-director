# Action requirements matrix

| Action | Provider requirements | Content requirements | Dimension | Authority | Current resolution |
|---|---|---|---|---|---|
| NO_ACTION | none | none | current/none | observe | always |
| SPAWN_ACTOR / DESPAWN_ACTOR | ACTOR_SOURCE + lifecycle | actor descriptor | explicit target | Director-owned | CustomNPCs |
| SPAWN_THREAT / DESPAWN_THREAT | THREAT_SOURCE + lifecycle | threat descriptor | explicit target | Director-owned | SlenderMan |
| PLACE_STRUCTURE | structure + block mutation | allowlisted structure | explicit target | Director-owned mutation | GraveStone |
| ROLLBACK_STRUCTURE | rollback safe | journal entry | journal dimension | Director-owned mutation | GraveStone |
| AMBUSH/BOSS | behavior + target + lifecycle | threat/actor | explicit | stronger policy | unavailable |
| item/equipment actions | item/equipment provider | item descriptor | explicit if spatial | ownership policy | unavailable |
