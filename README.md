# Sobrenatural Director

Sobrenatural Director is a Minecraft 1.7.10 Director foundation for coordinating contextual narrative decisions, provider capabilities, pacing, persistence and controlled gameplay boundaries.

## Platform

- Minecraft `1.7.10`
- Forge `10.13.4.1614`
- Java `8`
- Gradle wrapper under `director/gradle/`

The current implementation is an alpha foundation. Production safety, provider availability, persistence and execution authorization remain explicit boundaries. Optional or proprietary mods are not bundled in this repository.

## Decision Quality Benchmark

`DIRECTOR_BENCHMARK_V1` is a deterministic, offline, plan-only benchmark for measuring the current heuristic Director. It separates hard correctness and safety from decision quality, temporal/narrative quality and efficiency. It generates reproducible single-step scenarios and sequences, exports JSON/JSONL traces, and provides a fixed composite score for future comparisons.

Run it from `director/` with Java 8:

```bash
./gradlew test benchmarkFoundation
```

The current baseline is recorded as `NON_FINAL_CANDIDATE` because release hardening and integrated-client/Pojav validation are still open. Benchmark output is in `runtime-tests/benchmark/`; design and scoring documentation is in `analysis/benchmark/`.

## ML Workspace

`ml/` is reserved for future offline model experiments. Kaggle is the intended environment for future feature analysis and training. No neural network, reinforcement learning policy or learned scorer is implemented yet. TEST and HOLDOUT benchmark rows must not be used as training input.

## Repository Contents

This repository should contain source code, benchmark tooling/schemas, reproducible benchmark outputs and documentation only. Minecraft worlds, saves, runtime logs, build output, Forge caches, original/proprietary mod JARs, credentials, tokens, datasets and checkpoints remain local and are excluded by `.gitignore`.

No proprietary mod, original mod asset or proprietary provider implementation is included in the GitHub/Kaggle workspace.
