# ML Workspace

This directory is reserved for future offline model experiments. The current benchmark is the source of truth for feature schema and baseline comparison:

- `runtime-tests/benchmark/director-benchmark-v1-baseline.json`
- `runtime-tests/benchmark/director-benchmark-v1-features-A.jsonl`
- `analysis/benchmark/DIRECTOR_BENCHMARK_FUTURE_ML_INTEGRATION.md`

The leak-free V2 pipeline is implemented by `build_dataset.py`, `data.py`,
`model.py`, `train.py`, and `checkpointing.py`. It fits preprocessing on TRAIN
only and refuses incompatible checkpoints. Run it with a V2 JSONL source and
an output directory outside the repository; TEST and HOLDOUT are evaluation
only. The first scorer is a small embedding MLP in shadow mode and does not
alter Director runtime decisions.
