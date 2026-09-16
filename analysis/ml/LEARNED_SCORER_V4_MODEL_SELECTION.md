# Learned Scorer V4 model selection

Selection was validation-only using `0.6 * IID regret + 0.4 * STRESS
regret`, with seed variance and parameter count as tie-breakers.

| family/objective | params | IID regret mean±sd | stress regret mean±sd |
|---|---:|---:|---:|
| MLP [64], pairwise | 3,625 | 0.01944±0.01183 | 0.02571±0.01174 |
| MLP [64], hybrid | 3,625 | 0.04035±0.02098 | 0.05180±0.01975 |
| MLP [128,64], pairwise | 15,209 | 0.00600±0.00276 | 0.00931±0.00423 |
| MLP [256,128,64], pairwise, probe seed 7 | 54,761 | 0.00215 | 0.00229 |

The medium pairwise model was selected because it improved all three required
seeds without the large model's incomplete seed study and retained a compact
Java-exportable shape. Final frozen model: MLP `[128,64]`, 15,209 parameters,
pairwise objective, seed 17. No Cross Network was reopened.

On the fresh blind, selected vs small baseline regret was 0.00910 vs 0.02375
(TEST), and 0.00428 vs 0.01256 (HOLDOUT). Paired state bootstrap regret deltas
were -0.01465 and -0.00827, with 95% CIs excluding zero. This is synthetic
teacher evidence, not real gameplay evidence.
