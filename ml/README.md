# ML Workspace

This directory is reserved for future offline model experiments. The current benchmark is the source of truth for feature schema and baseline comparison:

- `runtime-tests/benchmark/director-benchmark-v1-baseline.json`
- `runtime-tests/benchmark/director-benchmark-v1-features-A.jsonl`
- `analysis/benchmark/DIRECTOR_BENCHMARK_FUTURE_ML_INTEGRATION.md`

Training is not implemented. TEST and HOLDOUT rows must not be used as training input. Keep datasets, credentials, checkpoints and generated artifacts outside Git unless a reviewed, small, public fixture is intentionally added.
