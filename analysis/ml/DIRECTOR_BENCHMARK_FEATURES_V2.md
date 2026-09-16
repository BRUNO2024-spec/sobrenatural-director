# DIRECTOR_BENCHMARK_FEATURES_V2

One JSON object per candidate observation. `stateFeatures` and
`candidateFeatures` are the scorer input; `target` is supervision and is never
fed to the model.

| field | type | source | timing | role | training allowed? | leakage risk |
|---|---|---|---|---|---|---|
| schemaVersion | string | exporter constant | pre | metadata | yes | none |
| scenarioId, seed, split, trainingAllowed | string/int/string/bool | Scenario | pre | metadata | yes (split policy applies) | split misuse |
| candidateId | string | CandidateAction | pre | identity | yes | none |
| stateFeatures.hardValid/noAction/providerAvailable/continuation/recentHigh/dimension | bool/bool/bool/bool/int/int | Scenario and deterministic legality | pre | input | yes | do not replace safety gates |
| stateFeatures.environment | enum string | Scenario | pre | input | yes | vocab drift |
| candidateFeatures.candidateKind/intent/safety/provider | enum string | CandidateAction | pre | input | yes | must not use selected values |
| candidateFeatures.baseUtility | float | CandidateAction | pre | input | yes | heuristic proxy, not final target |
| target.quality | float | six pre-decision candidate metadata components | derived supervision | target | TRAIN labels only | never input |
| target.chosen | bool | policy selected id | post | target | TRAIN labels only | never input |

The exporter is deterministic, preserves the original scenario split function,
and emits every legal candidate observation. V1 files remain unchanged for
historical reproduction.
