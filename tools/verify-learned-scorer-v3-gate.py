"""Fail-closed verifier for the V3 blind cross-feature evaluation gate."""
import sys
from pathlib import Path
p=Path(sys.argv[1] if len(sys.argv)>1 else 'runtime-tests/benchmark/learned-scorer-v3-cross-evaluation.properties');v={}
for line in p.read_text().splitlines():
    if '=' in line:k,x=line.split('=',1);v[k]=x
required=('DATASET_LOCK','BLIND_EVAL_SPEC_FREEZE','MODEL_SELECTION','MODEL_FREEZE','BLIND_EVAL_GENERATED_AFTER_FREEZE','BLIND_TEST_EVALUATION','BLIND_HOLDOUT_EVALUATION','CROSS_ABLATION','NO_ACTION_AUDIT','TEMPORAL_AUDIT','SLICE_ANALYSIS','BOUNDARY_ANALYSIS','CPU_BENCHMARK','CHECKPOINT_SYNC','CHECKPOINT_SHA_MATCH','RESUME_VALIDATED','DETERMINISTIC_INFERENCE','CPU_GPU_PARITY','JAVA_EXPORT','PYTHON_GOLDEN_PARITY','JAVA_GOLDEN_PARITY','PYTHON_TESTS','JAVA_REGRESSION','GIT_DIFF_CHECK')
bad=[k for k in required if v.get(k)!='PASS']
for k in ('OLD_TEST_USED_FOR_MODEL_SELECTION','OLD_HOLDOUT_USED_FOR_MODEL_SELECTION','POST_BLIND_TEST_MODEL_CHANGES','POST_BLIND_HOLDOUT_MODEL_CHANGES'):
    if v.get(k)!='NO':bad.append(k)
for k in ('TRAIN_OVERLAP','VALIDATION_OVERLAP','OLD_TEST_OVERLAP','OLD_HOLDOUT_OVERLAP','BLIND_CANDIDATE_OVERLAP','TARGET_CONFLICTS'):
    if v.get(k)!='0':bad.append(k)
if v.get('BLIND_EVAL_GENERATED_AFTER_FREEZE')!='YES':bad.append('blind-order')
if int(v.get('GOLDEN_VECTOR_COUNT','0'))<64:bad.append('golden-count')
if v.get('FINAL_GATE')!='PASS':bad.append('FINAL_GATE')
if bad:print('gate failed: '+','.join(bad));sys.exit(1)
print('gate PASS')
