# Frozen HOLDOUT evaluation

HOLDOUT was opened only after TEST and with the identical frozen model.
`HOLDOUT_USED_FOR_TUNING=NO` and `POST_HOLDOUT_MODEL_CHANGES=NO`.

Rows: 340; groups: 120.

| metric | learned scorer |
|---|---:|
| MSE | 0.006121305 |
| RMSE | 0.078238772 |
| MAE | 0.060450943 |
| Pearson | 0.874862109 |
| Spearman | 0.858003148 |
| pairwise accuracy | 0.978979 |
| MRR | 0.970833 |
| NDCG | 0.999878 |
| mean regret | 0.000486 |
| predicted-best target quality | 0.791181 |

The heuristic comparison on HOLDOUT was mean target quality 0.747431 and mean
regret 0.044236. Despite this favorable synthetic comparison, seed variance
in the search was material, so shadow readiness remains
`INSUFFICIENT_EVIDENCE`, not a runtime recommendation.
