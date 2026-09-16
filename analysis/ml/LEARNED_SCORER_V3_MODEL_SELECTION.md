# V3 cross-feature model selection

Selection used TRAIN/VALIDATION only. The 12-run matrix compared MLP,
explicit semantic crosses, and one/two-layer Cross Network at approximately
the same compact budget, with seeds 7/17/27. The MLP baseline had 3,625
parameters; explicit crosses had 5,161; Cross Network had 3,727/3,829.

Seed variance was material. Cross Network depth 1/2 and explicit crosses did
not provide a consistent regret improvement over MLP across seeds. Therefore
the simplest MLP was frozen; this is a valid `NO_CLEAR_GAIN` result, not a
claim that crossing is useless. No old TEST/HOLDOUT value influenced the
decision. The official blind corpus is generated only after this freeze.

`DO_EXPLICIT_CROSSES_HELP=NO_CLEAR_GAIN`
`DOES_CROSS_NETWORK_HELP=NO_CLEAR_GAIN`
`IS_GAIN_EXPLAINED_ONLY_BY_MORE_PARAMETERS=INCONCLUSIVE`
