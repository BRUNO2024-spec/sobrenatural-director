# Splits

V1 uses deterministic family/seed generation with `TRAIN=60%`, `VALIDATION=20%`, `TEST=10%`, and `HOLDOUT=10%`. Split assignment is derived from the stable seed modulo ten. Future training may use TRAIN, validation may tune parameters, and TEST/HOLDOUT are never exported as training input by default.

The leakage audit requires distinct scenario ids/fingerprints and family/seed generation independent of policy output. The current run records `BENCHMARK_SPLIT_LEAKAGE_AUDIT=PASS`; any catalog change requires `DIRECTOR_BENCHMARK_V2` rather than silently changing V1.
