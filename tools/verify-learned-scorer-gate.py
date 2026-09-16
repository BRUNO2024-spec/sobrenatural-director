import sys
from pathlib import Path

required = "RUN_ID SOURCE_COMMIT FEATURE_SCHEMA V1_LEAKAGE_CONFIRMED V2_LEAKAGE_FREE V2_DETERMINISM SPLIT_LEAKAGE TRAIN_ONLY_FIT TEST_USED_FOR_TRAINING HOLDOUT_USED_FOR_TRAINING LOCAL_TESTS GRADLE_TESTS KAGGLE_DOCTOR CUDA GPU_COUNT GPU_SMOKE CHECKPOINT_LATEST CHECKPOINT_BEST CHECKPOINT_SYNC CHECKPOINT_SHA_MATCH RESUME_VALIDATED PRODUCTION_RUNTIME_BEHAVIOR_CHANGED PROTECTED_WORLD_TOUCHED SECRETS_COMMITTED FINAL_GATE".split()
path = Path(sys.argv[1] if len(sys.argv) > 1 else "runtime-tests/benchmark/learned-scorer-v1-foundation.properties")
values = {}
for line in path.read_text().splitlines():
    if "=" in line:
        k,v=line.split("=",1); values[k]=v
missing=[k for k in required if not values.get(k) or values[k] == "NOT_RUN" or values[k].startswith("FAIL") or values[k].startswith("PARTIAL")]
if missing:
    print("gate failed: " + ",".join(missing)); sys.exit(1)
if values.get("FINAL_GATE") != "PASS": print("gate failed: FINAL_GATE"); sys.exit(1)
print("gate PASS")
