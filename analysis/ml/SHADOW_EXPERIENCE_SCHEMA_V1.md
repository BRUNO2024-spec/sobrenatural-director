# DIRECTOR_SHADOW_EXPERIENCE_V1

Observation-only, local JSONL schema. Records are factual and versioned; they
are not rewards, preferences, or training labels. `trainingAllowed` is always
`false` in this staging version.

Events are joined by opaque local `sessionId`, `decisionId`, and
`executionId`: `SESSION_START`, `DECISION_SNAPSHOT`, `ACTUAL_DECISION`,
`SHADOW_SCORE_RESULT`, lifecycle events, `OUTCOME_CLOSE`, and `SESSION_END`.
World objects never cross the worker boundary. Player names, raw UUIDs, IP,
chat, and network destinations are excluded.

Incomplete sessions/outcomes are retained with an explicit reason and are
never silently repaired by offline ingestion.
