# Future ML Integration

Future policies implement `DirectorDecisionPolicy` and receive immutable context plus candidate actions. A learned scorer may rank legal candidates, but hard safety, provider validity, dimensions, stale/terminal checks and final execution authorization remain external and absolute.

Future comparison must use the same V1 TEST and HOLDOUT inputs without exporting them into training: `CURRENT_HEURISTIC_DIRECTOR` versus `LEARNED_MODEL_V1`. Report `BASELINE_COMPOSITE_SCORE`, `MODEL_COMPOSITE_SCORE`, `ABSOLUTE_IMPROVEMENT`, `PERCENTAGE_IMPROVEMENT` and `DECISION_QUALITY_RELATIVE_IMPROVEMENT`, alongside every hard, temporal, efficiency and safety metric.

The optional JSONL feature schema is `DIRECTOR_BENCHMARK_FEATURES_V1`. It is not training data until explicitly filtered to TRAIN. No neural network, reinforcement learning, GPU dependency or production scoring change is part of V1.
