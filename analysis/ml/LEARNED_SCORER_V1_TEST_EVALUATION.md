# Frozen TEST evaluation

Evaluation was run after `MODEL_FREEZE=PASS` using the frozen configuration
only. `TEST_USED_FOR_TUNING=NO` and `POST_TEST_MODEL_CHANGES=NO`.

Rows: 366; groups: 120.

| metric | learned scorer |
|---|---:|
| MSE | 0.008299758 |
| RMSE | 0.091103005 |
| MAE | 0.069392247 |
| Pearson | 0.835801978 |
| Spearman | 0.906350738 |
| pairwise accuracy | 1.000000 |
| MRR | 1.000000 |
| NDCG | 1.000000 |
| mean regret | 0.000000 |
| predicted-best target quality | 0.808750 |

The heuristic comparison on TEST was mean target quality 0.808333 and mean
regret 0.000417. This is an offline synthetic target comparison, not evidence
of production improvement.
