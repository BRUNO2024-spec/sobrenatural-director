# Learned Scorer V1 full-training plan

## Objective and data contract

The scorer predicts the V2 `target.quality` for each candidate using only the
pre-decision state and candidate features. Candidate rows sharing `scenarioId`
form a ranking group. `target.chosen` is supervision only and is never an
input. Hard safety remains deterministic and external.

## Protocol

* TRAIN (1,997 rows): preprocessing fit, gradient updates and target statistics.
* VALIDATION (653 rows): model selection, early stopping and all tuning.
* TEST (366 rows): one frozen evaluation only.
* HOLDOUT (340 rows): one separate frozen confirmation after TEST.

Primary selection metric is validation group pairwise ranking accuracy; ties
are resolved by lower validation regret, then smaller parameter count, then
lower CPU inference cost. Regression metrics (MSE, RMSE, MAE, Pearson and
Spearman) are secondary diagnostics. Ranking is valid because V2 retains
multiple candidate observations per scenario.

## Baselines and search

Baseline 0 predicts the TRAIN target mean. Baseline 1 reproduces the current
heuristic by selecting the row with `target.chosen=true`; it is evaluated only
as an offline comparator, never as an input. The predeclared compact search is
hidden `{16,32,64}`, layers `{1,2}` (implemented as hidden width variants),
dropout `{0.0,0.1}`, learning rate `{0.001,0.003}`, weight decay `{0,1e-4}`
and seeds `{7,17,27}`. Runs use TRAIN+VALIDATION only and stop after a fixed
small epoch budget with validation selection.

## Freeze and locked evaluation

The best validation configuration is written to a frozen config with exact
feature order, vocabularies, normalization, architecture, optimizer and seed.
Its SHA-256 and freeze commit are recorded before TEST is opened. No config,
architecture, epoch or threshold changes are permitted after that marker.
The final frozen model remains TRAIN-only to avoid changing the already-fit
manifest; this makes the locked evaluations directly comparable.

## Stop conditions and honesty

Stop if leakage, split overlap, NaN/Inf, checkpoint incompatibility or failed
determinism is found. Report `CLEAR_IMPROVEMENT`, `NO_CLEAR_DIFFERENCE`,
`REGRESSION` or `INSUFFICIENT_EVIDENCE`; a lower regression loss alone does not
qualify as an improvement over the heuristic.
