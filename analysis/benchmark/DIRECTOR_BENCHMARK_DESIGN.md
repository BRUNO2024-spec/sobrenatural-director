# Director Decision Quality Benchmark Foundation

Version: `DIRECTOR_BENCHMARK_V1`.

This is an offline, deterministic, plan-only benchmark. It never loads Forge, mutates worlds, spawns entities, force-loads chunks, invokes MPE, or applies natural pressure. It measures the current heuristic through `CurrentHeuristicDirectorPolicy` and leaves a neutral `DirectorDecisionPolicy` boundary for future learned scoring or RL policies.

The reference expectations are independent semantic constraints and multi-objective quality attributes. The expected answer is not simply the current engine's choice. Hard correctness is evaluated separately from relevance, continuity, novelty, pacing and efficiency.

The baseline uses the candidate artifact SHA recorded in the machine-readable result and is `NON_FINAL_CANDIDATE` while release hardening remains open. The benchmark is not a replacement for Forge, Pojav, or release validation.
