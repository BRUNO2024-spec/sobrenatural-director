# Kaggle Training Worker Handoff

This handoff publishes the audited workspace baseline for a future Kaggle
training worker. Training has NOT started in this publication step.

## Published Source

```text
GITHUB_REPOSITORY=BRUNO2024-spec/sobrenatural-director
GITHUB_CLONE_URL=https://github.com/BRUNO2024-spec/sobrenatural-director.git
PUBLISHED_BRANCH=main
BASE_PUBLISHED_COMMIT=c72f0682e172d6eb076124e274a84cd19f3dfbd3
```

The Kaggle workspace is expected at:

```text
/kaggle/working/SOBRENATURAL-DIRECTOR
```

Clone and pin the worker to the exact published commit supplied in the final
publication report:

```bash
cd /kaggle/working
git clone https://github.com/BRUNO2024-spec/sobrenatural-director.git SOBRENATURAL-DIRECTOR
cd /kaggle/working/SOBRENATURAL-DIRECTOR
git checkout <PUBLISHED_COMMIT_SHA>
git rev-parse HEAD
git status --short
```

## Benchmark Baseline

```text
BENCHMARK_VERSION=DIRECTOR_BENCHMARK_V1
BENCHMARK_BASELINE_RUN_ID=benchmark-baseline-20260915-01
BASELINE_COMPOSITE_SCORE=77.4938
HARD_CORRECTNESS_SCORE=94.5833
DECISION_QUALITY_SCORE=76.2646
TEMPORAL_QUALITY_SCORE=36.6138
EFFICIENCY_SCORE=94.5833
HARD_SAFETY_VIOLATIONS=0
CANDIDATE_IDENTIFIER=e64831aca44983b3d7f013ff0410462fea39690ba89f0e0a0e16a60b4215cb38
RELEASE_STATUS=NON_FINAL_CANDIDATE
```

The baseline is a reference artifact. Do not alter these values when setting
up the future worker.

## Expected Benchmark Files

The pinned checkout must contain the existing benchmark implementation and
artifacts in:

- `director/src/main/java/com/sobrenaturaldirector/benchmark/`
- `director/src/test/java/com/sobrenaturaldirector/DirectorBenchmarkFoundationTest.java`
- `director/src/test/java/com/sobrenaturaldirector/DirectorBenchmarkScenarioTest.java`
- `analysis/benchmark/`
- `runtime-tests/benchmark/`
- `analysis/ml/KAGGLE_TRAINING_WORKER_HANDOFF.md`

## Kaggle Environment Checks

Hardware previously observed independently was 2 x Tesla T4. That prior
observation is not a new Kaggle execution in this publication step. Run:

```bash
pwd
python3 --version
nvidia-smi
```

```bash
python3 - <<'PY'
import torch

print("PyTorch:", torch.__version__)
print("CUDA:", torch.cuda.is_available())
print("GPU_COUNT:", torch.cuda.device_count())

for i in range(torch.cuda.device_count()):
    p = torch.cuda.get_device_properties(i)
    print(i, torch.cuda.get_device_name(i))
    print("VRAM_GB:", round(p.total_memory / 1024**3, 2))
PY
```

Expected from the prior independent validation only:

```text
CUDA=True
GPU_COUNT=2
GPU 0=Tesla T4
GPU 1=Tesla T4
```

## Future Training Rules

- Training may use only the `TRAIN` split.
- `VALIDATION` is for tuning and evaluation during development only.
- `TEST` and `HOLDOUT` must never enter training or tuning.
- Future ML code belongs under `ml/`; no ML pipeline is implemented here.
- Checkpoints must be portable and reproducible across environments.
- Do not depend exclusively on the temporary Kaggle session filesystem; export
  required artifacts to durable storage.
- Never commit checkpoints, tokens, API keys, credentials, or Jupyter URLs.
- Training has NOT started as part of this handoff or publication.

Release hardening remains separate and unchanged:

```text
PARTIAL_FINAL_INTEGRATION_LONG_RUN_RELEASE_HARDENING
PRIMARY_CLIENT_INTEGRATED_RUNTIME_UNAVAILABLE
```
