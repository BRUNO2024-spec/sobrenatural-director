# Learned Scorer V2 massive evaluation

```text
RUN_ID=scorer-v2-final-20260916
DATASET_LOCK=PASS
MODEL_FREEZE=PASS
```

The locked V3 corpus was consumed without regeneration: 60,000 state groups,
314,715 rows, and the published manifest SHA
`10de28923e0251d67befd53b25400c781f7e6315d96250d5db616e69eb788b7d`.

The controlled objective study found pairwise ranking preferable on validation
regret to the tested pointwise alternatives. The selected compact model has
3,625 parameters: categorical embeddings (dimension 4), one 64-unit ReLU
layer, no dropout, Adam, learning rate 0.001, and seed finalists 7/17/27.
Validation mean regret for the representative pairwise-64 run was 0.00679;
the seed-7 frozen run reached 0.01541 at its selected checkpoint.

Locked seed-7 evaluation was TEST pairwise 0.81494, top-1 0.69137, NDCG
0.98948, mean regret 0.02163; HOLDOUT pairwise 0.82110, top-1 0.67138,
NDCG 0.98915, mean regret 0.02435. These are benchmark-teacher imitation
results, not gameplay or player-preference evidence.

The one-versus-two GPU microbenchmark measured 0.389 s versus 0.806 s for 20
steps (speedup 0.49x), so one GPU is selected. The V2 frozen config is
`ml/configs/learned_scorer_v2_frozen.json` (SHA
`45b174fbe1ad166f4dd42a486d0dc5ec3cd2beaa72b861c3a2c1e31f133d9cd4`) and the
checkpoint is persisted outside Git under the learned-scorer-v2 checkpoint
directory.

Engineering closure is now complete without changing the frozen model. A real
two-process restart restored step 31, continued to step 32, restored Adam and
Python/NumPy/torch CPU/CUDA RNG state, and passed compatibility guards.
Diagnostic VALIDATION masks, post-freeze slices, NO_ACTION analysis, sequence
analysis, boundary diagnostics, CPU timing, CPU/GPU parity, deterministic
export, and 64-vector Python/Java 8 parity are recorded in the closure
artifacts. Java pure inference is feasible for this MLP.

The closure gate is PASS, while Shadow Mode readiness remains
`INSUFFICIENT_EVIDENCE`: results imitate a synthetic deterministic teacher and
do not establish real gameplay utility or player preferences. Production
runtime, hard safety, protected world, and Shadow Mode remain unchanged.
