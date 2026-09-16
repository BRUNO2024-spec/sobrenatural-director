# Learned Scorer V4 foundation — final report

```text
CLASSIFICATION=PASS_LEARNED_SCORER_V4_GENERALIZATION_CURRICULUM_FOUNDATION
RUN_ID=experience-v4-20260916
FINAL_GATE=PASS
```

The V3 blind corpus is permanently marked `CONSUMED_DIAGNOSTIC_DATA` and was
not copied into V4. The postmortem supports a multifactor gap: coverage,
composition, temporal context, and generator artifacts have moderate evidence;
margin ambiguity, candidate-set shift, and capacity have weak evidence. The
teacher observability audit found 16/16 signals represented by legal
pre-decision inputs, so no shortcut feature was added.

V4 uses the unchanged semantic input family (`DIRECTOR_EXPERIENCE_FEATURES_V4`)
and a fresh curriculum. The final corpus has 120,000 states, 629,511 rows,
2,000 intact episodes, 65 shards, zero historical input/candidate overlap, and
zero structural audit errors. TRAIN/IID/STRESS contain 95,998/502,365,
12,002/60,465, and 12,000/66,681 state/row groups.

The future blind spec is frozen (`DIRECTOR_V4_BLIND_EVAL_V1`) but its dataset
was neither generated nor opened. Kaggle manifest SHA matched the local
manifest. Python unittest: 10 passed. Java regression: 295 tests passed.
Runtime Java, hard safety, protected world, original mods, and Shadow Mode
were untouched. Full V4 training was not performed and no release-final claim
is made.

`SUPPORT_COVERAGE_IMPROVEMENT=NO_CLAIM`: the diagnostic showed a slight p50
distance worsening, despite a slight p95 improvement. This negative result is
intentional and documented.
