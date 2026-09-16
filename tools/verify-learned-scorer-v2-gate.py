"""Fail-closed verifier for the V2 offline evaluation gate."""
import sys
from pathlib import Path

p=Path(sys.argv[1] if len(sys.argv)>1 else "runtime-tests/benchmark/learned-scorer-v2-massive-evaluation.properties")
v={}
for line in p.read_text().splitlines():
    if "=" in line:
        k, value=line.split("=", 1); v[k]=value
required=("DATASET_LOCK", "MODEL_FREEZE", "TEST_EVALUATION", "HOLDOUT_EVALUATION", "CHECKPOINT_SYNC", "CHECKPOINT_SHA_MATCH", "RESUME_VALIDATED", "PYTHON_TESTS", "GIT_DIFF_CHECK")
bad=[k for k in required if v.get(k) != "PASS"]
for k in ("TEST_USED_FOR_TRAINING", "TEST_USED_FOR_TUNING", "HOLDOUT_USED_FOR_TRAINING", "HOLDOUT_USED_FOR_TUNING"):
    if v.get(k) != "NO": bad.append(k)
if v.get("FINAL_GATE") != "PASS": bad.append("FINAL_GATE")
if bad:
    print("gate failed: " + ",".join(bad)); sys.exit(1)
print("gate PASS")
