# V3 supervision provenance

`teacherQuality` is produced by `ml/generate_experience_v3.py::quality` and
`teacherChosen` is the deterministic argmax of that teacher over the eligible
candidate set. The teacher is offline, deterministic, and does not access
Minecraft, worlds, entities, future steps, or execution outcomes.

```text
TARGET_PROVENANCE=DETERMINISTIC_BENCHMARK_TEACHER
SYNTHETIC_SUPERVISION=YES
REAL_GAMEPLAY_OUTCOME_SUPERVISION=NO
```

This corpus measures learnability of the benchmark teacher objective, not real
player behavior, gameplay utility, or a production improvement. Hard-invalid
candidates are excluded from scoring candidates; safety remains deterministic.
