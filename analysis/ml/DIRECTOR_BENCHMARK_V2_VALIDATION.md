# V2 validation record

The V2 exporter is isolated in `DirectorBenchmarkRunner`; V1 export names and
semantics are retained. It emits 3,356 candidate observations from the 1,200
benchmark decisions (candidate cardinality varies because continuation and
no-action candidates are scenario-dependent).

* A/B byte equality: PASS
* V2 schema: `DIRECTOR_BENCHMARK_FEATURES_V2`
* rows: TRAIN 1,997; VALIDATION 653; TEST 366; HOLDOUT 340
* `trainingAllowed`: true only on TRAIN (1,997 rows)
* target present in input JSON: NO
* scenario IDs and candidate IDs are deterministic; no world/API mutation
* V1 historical files remain the compatibility baseline

The local Python pipeline built a manifest successfully with 7 numeric input
features and vocabularies of 7/5/6/3/4 values for environment, candidate kind,
intent, safety and provider. Normalization was fit from TRAIN only.

The repository Gradle wrapper could not be run on this host because its legacy
launcher rejects the installed Java `21.0.12` before evaluating the project.
This is an environment/toolchain limitation, not a benchmark failure; no
production runtime behavior was changed.
