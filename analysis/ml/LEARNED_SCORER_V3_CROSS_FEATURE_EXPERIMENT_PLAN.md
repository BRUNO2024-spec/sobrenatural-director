# Learned Scorer V3 cross-feature experiment plan

This phase is TRAIN/VALIDATION-only until model freeze. The observed V2
weaknesses are sequence regret, narrative/environment slices, and boundary
cases; the frozen V2 model and the old TEST/HOLDOUT are historical references,
not selection data. Primary metric is validation mean regret, with pairwise,
top-1, seed variance, parameter count, and CPU cost as tie-breakers.

The controlled matrix has three families: V2-style MLP baseline, a fixed small
explicit semantic-cross model, and a simple one/two-layer cross network. All
use the same feature order, TRAIN normalization/vocabularies, pairwise objective,
and same-state pairs. No Transformer, arbitrary cross product, or parameter
expansion is planned. Finalists use seeds 7/17/27.

The new `DIRECTOR_CROSS_EVAL_V1` spec is frozen before tuning and its corpus is
not generated until after `MODEL_FREEZE=PASS`. Its state/episode/seed namespace
is disjoint from every V3 split. Old TEST/HOLDOUT may only be reported as
historical evidence.
