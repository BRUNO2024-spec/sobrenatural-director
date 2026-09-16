# Learned Scorer V3 cross-feature evaluation

```text
CLASSIFICATION=PASS_LEARNED_SCORER_V3_CROSS_CONTEXT_OFFLINE_EVALUATION
RUN_ID=scorer-v3-cross-20260916
FINAL_GATE=PASS
```

The blind evaluation spec was frozen before tuning (SHA
`e7d13712feffefa33f918b134bc17f138b85ca418eee34aea088a528567f50e6`). The
selected no-cross MLP was frozen in commit `45d366f` with config SHA
`18c334e51c5a45e8844a283478c44ef2d8fba80a48e5fe00fab2dcc21b82d41d`.

The 12-run TRAIN/VALIDATION matrix compared MLP, explicit semantic crosses,
and Cross Network depths 1/2 using seeds 7/17/27. Crosses did not provide a
consistent validation improvement, so the simplest 3,625-parameter MLP was
selected (`NO_CLEAR_GAIN`). Old TEST/HOLDOUT were not used for selection.

After freeze, `DIRECTOR_CROSS_EVAL_V1` was generated with 15,000 TEST and
15,000 HOLDOUT state groups, 78,680 rows each. Input and candidate overlap
against every old V3 split were zero. Blind TEST pairwise/top-1/regret were
`0.73239/0.58693/0.04500`; HOLDOUT were `0.73286/0.58900/0.04477`.

This proves only synthetic deterministic-teacher generalization. It does not
prove real gameplay or player-preference generalization. Shadow Mode remains
disabled and readiness remains `INSUFFICIENT_EVIDENCE`.

The final checkpoint (`cd439945855539ed4a8d9e62c7296801b6ab7f071dcb415fcbf46ca1bad3340a`)
contains frozen provenance, preprocessing, optimizer state, and atomic-write
metadata. The V3 checkpoint restart proof restored the saved step and advanced
one step in a new process. No production JAR or Minecraft runtime source was
changed.
