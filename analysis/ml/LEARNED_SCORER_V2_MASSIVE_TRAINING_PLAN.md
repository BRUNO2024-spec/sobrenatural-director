# Learned Scorer V2 massive training plan

## Locked data and objective

This phase consumes the existing V3 corpus without regeneration. The scorer
maps the 19 numeric and 8 categorical pre-decision features of one candidate
to `teacherQuality`; rows sharing `stateId` form ranking groups. The target is
`DETERMINISTIC_BENCHMARK_TEACHER`, not a gameplay outcome. IDs, split metadata,
teacher fields and post-decision data remain outside inputs.

TRAIN is the only fit/update source. VALIDATION controls objective,
architecture, hyperparameters, early stopping and seed selection. TEST is
opened once after freeze, and HOLDOUT only after TEST. Neither can change any
decision. No runtime or safety path is involved.

## Baselines and controlled study

The study compares a TRAIN-mean predictor, a ridge/linear baseline, the V1
small MLP architecture retrained on V3, pointwise regression, pairwise
same-state ranking and a compact hybrid. Listwise is only attempted if a
stable grouped implementation is useful. Model sizes are small/medium/large
based on parameter count, not GPU availability. Finalists use seeds 7, 17 and
27; selection is by validation mean regret, then pairwise accuracy, seed
variance, parameter count and CPU latency.

The predeclared search is approximately 20 controlled runs: objectives
pointwise/pairwise/hybrid, widths 32/64/128 (with one larger 256/128 candidate),
dropout 0/0.1, learning rates 0.001/0.003, and weight decay 0/1e-4. No
Cartesian expansion is permitted. Ablations cover temporal/context,
provider-related and numeric-only inputs for the selected finalist.

## Freeze, final train and locked evaluation

The winner is written to `learned_scorer_v2_frozen.json` with exact manifest,
schema, feature order, vocabularies, normalization, objective and seed policy.
The SHA is recorded before TEST. Final training remains TRAIN-only with the
validation-selected epoch count, avoiding any post-freeze refit ambiguity.
TEST and HOLDOUT use that same checkpoint and configuration only.

## GPU and evidence policy

One-T4 and true two-GPU DDP/parallel measurements use identical representative
batches and synchronized steps. The faster strategy is selected; model size
will not be increased to consume GPUs. Metrics include regression, grouped
ranking, regret, slices, bootstrap intervals and seed variance. Excellent
teacher imitation is explicitly not real gameplay intelligence.
