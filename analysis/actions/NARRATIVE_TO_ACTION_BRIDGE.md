# Narrative-to-action bridge

| Goal | Blueprint/role | Action intent | Current result |
|---|---|---|---|
| INVESTIGATION | POI + WITNESS + CLUE_SOURCE | `CREATE_INVESTIGATION_SITE` | composition plan; clue item unavailable |
| INVESTIGATION | RUIN + VICTIM + DISCOVERY_TARGET | `PLACE_STRUCTURE` plus actor if resolved | plan-only; role requirements visible |
| DISCOVERY | POINT_OF_INTEREST | `PLACE_STRUCTURE` / `REGISTER_DISCOVERY` | structure path available; registration deferred |
| THREAT_EVENT | POINT_OF_INTEREST + THREAT | `CREATE_THREAT_PRESENCE` | threat lifecycle path available |
| AMBIENT_EVENT | POINT_OF_INTEREST | `NO_ACTION` or internal intent | no provider mutation implied |

Blueprint roles remain semantic. The bridge never hardcodes a provider ID.
