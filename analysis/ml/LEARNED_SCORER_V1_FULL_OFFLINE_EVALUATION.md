# Learned Scorer V1 full offline evaluation

## Run and freeze

* Run: `learned-scorer-full-offline-20260916`
* Dataset manifest SHA-256: `744f8da3f2ccde009602466a6c549bea1a17f88f9e72cd815d811ad8aae053a6`
* Frozen config SHA-256: `bfa338b00160010f744a4eadb5787369459615ae219d3a69fff0e801d11efe13`
* Search: 18 runs (6 compact configurations × seeds 7, 17, 27), TRAIN and
  VALIDATION only. Winner: hidden 32, dropout 0, Adam LR .003, weight decay 0,
  seed 7.
* Primary selection: validation group pairwise accuracy; tie breakers were
  lower regret, smaller model, then CPU cost. The frozen model has 1,029
  parameters and 25 full-batch epochs.

## Locked results

| split | MSE | RMSE | MAE | Pearson | Spearman | pairwise | MRR | NDCG | regret |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| VALIDATION | .004994277 | .070670200 | .052471330 | .876112 | .866872 | .988430 | .985417 | .999939 | .000243 |
| TEST | .008299758 | .091103005 | .069392247 | .835802 | .906351 | 1.000000 | 1.000000 | 1.000000 | 0 |
| HOLDOUT | .006121305 | .078238772 | .060450943 | .874862 | .858003 | .978979 | .970833 | .999878 | .000486 |

TEST was evaluated once after freeze; HOLDOUT was evaluated separately after
TEST. Neither was used for training, tuning, preprocessing or model changes.

## Baseline comparison

Baseline 0 (TRAIN mean `0.686572`) had validation MSE `.016640`, MAE
`.123742`, pairwise accuracy `.500000` and mean regret `.270521`; this confirms
the MLP is not merely a constant predictor. Baseline 1 is the current
heuristic policy and is shown below.

| split | learned mean target | heuristic mean target | learned regret | heuristic regret | agreement | learned better / heuristic better / tie |
|---|---:|---:|---:|---:|---:|---:|
| VALIDATION | .795278 | .750104 | .000243 | .045417 | .754167 | 59 / 0 / 181 |
| TEST | .808750 | .808333 | 0 | .000417 | .950000 | 6 / 0 / 114 |
| HOLDOUT | .791181 | .747431 | .000486 | .044236 | .733333 | 32 / 0 / 88 |

The target is synthetic and derived from pre-decision heuristic candidate
components. These results show offline ranking behavior on this benchmark,
not a product-quality or runtime improvement claim. Bootstrap paired 95%
intervals for learned-minus-heuristic mean target quality were [0.031875,
0.059653] validation, [0.000069, 0.000833] test and [0.025972, 0.062083]
holdout (2,000 deterministic resamples).

## Readiness conclusion

The learned scorer is better than the heuristic on these locked synthetic
ranking comparisons, but the three-seed search showed material variance (the
winner seed 7 was substantially stronger than seeds 17 and 27). Therefore
`SHADOW_MODE_READINESS=INSUFFICIENT_EVIDENCE`: infrastructure and evaluation
are complete, but no runtime shadow enablement is authorized by this phase.

## Safety and cost

Hard safety remains outside the model. The chosen device was one Tesla T4;
true `DataParallel` measurement was slower than one GPU (0.092119 s versus
0.011750 s for 30 batches). CPU batch inference was p50 0.783 ms, p95 0.896
ms and p99 0.931 ms. No PyTorch runtime was added to Minecraft.
