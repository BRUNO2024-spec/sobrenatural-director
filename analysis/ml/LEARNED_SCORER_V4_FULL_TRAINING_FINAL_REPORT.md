# Learned Scorer V4 full training and fresh blind evaluation

```text
CLASSIFICATION=PASS_LEARNED_SCORER_V4_FULL_FRESH_BLIND_EVALUATION
RUN_ID=v4-final-20260916
FINAL_GATE=PASS
```

The selected frozen model is an MLP `[128,64]` with 15,209 parameters and a
pairwise objective. The final training used TRAIN only for four frozen epochs;
validation was used only before freeze. Freeze commit: `c322b2a`; final
artifact tooling commit: `ef35451`.

Fresh blind `DIRECTOR_V4_BLIND_EVAL_V1` contains 20,000 TEST and 20,000
HOLDOUT states. Selected TEST metrics: pairwise 0.83794, top-1 0.80365,
regret 0.00910. HOLDOUT: pairwise 0.86362, top-1 0.85970, regret 0.00428.
The paired small-baseline improvements were significant by state bootstrap.

Checkpoint restart/resume passed at steps 1→2. CPU/GPU max absolute
difference was `5.22e-8`; Java 8 parity over 64 vectors had max difference
`3.02e-8`. Typical CPU p50 was `0.325 ms`; selected model has 15,209
parameters. Worst reported family slice was TEST `LOW_IMPACT_CONTINUATION`
(regret 0.01475) and HOLDOUT `RECOVERY_REQUIRED` (0.00939).

The fresh synthetic result is materially better than the historical V3 blind
comparison, but it does not establish gameplay generalization. Shadow Mode
therefore remains disabled and readiness is `INSUFFICIENT_EVIDENCE`.
