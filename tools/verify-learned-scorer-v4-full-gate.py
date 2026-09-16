from pathlib import Path
import sys

def main():
    data = dict(line.split('=', 1) for line in Path(sys.argv[1]).read_text().splitlines() if line and not line.startswith('#'))
    keys = ('DATASET_LOCK','FUTURE_BLIND_SPEC_INTEGRITY','BASELINE_MLP_3625','OBJECTIVE_STUDY','CAPACITY_STUDY','SEED_STUDY','MODEL_SELECTION','MODEL_FREEZE','FINAL_TRAIN','CHECKPOINT_SYNC','CHECKPOINT_SHA_MATCH','RESUME_PROCESS_RESTART','RESUME_GLOBAL_STEP_CONTINUITY','RESUME_VALIDATED','BLIND_TEST_EVALUATION','BLIND_HOLDOUT_EVALUATION','PAIRED_BOOTSTRAP','CPU_BENCHMARK','CPU_GPU_PARITY','JAVA_EXPORT','JAVA_GOLDEN_PARITY','PYTHON_TESTS','JAVA_REGRESSION')
    bad = [key for key in keys if data.get(key) != 'PASS']
    bad += [key for key in ('ALL_HISTORICAL_INPUT_OVERLAP','V4_DEVELOPMENT_INPUT_OVERLAP','CANDIDATE_OVERLAP','EPISODE_OVERLAP','TARGET_CONFLICTS') if data.get(key) != '0']
    bad += [key for key in ('FUTURE_BLIND_DATASET_EXISTS_BEFORE_FREEZE','POST_BLIND_TEST_MODEL_CHANGES','POST_BLIND_HOLDOUT_MODEL_CHANGES','SHADOW_MODE_ENABLED','RELEASE_FINAL_DECLARED') if data.get(key) != 'NO']
    bad += [key for key in ('BLIND_GENERATED_AFTER_MODEL_FREEZE','FINAL_GATE') if data.get(key) not in ('YES','PASS')]
    print('gate PASS' if not bad else 'gate FAIL: ' + ', '.join(bad))
    return 0 if not bad else 1

if __name__ == '__main__':
    raise SystemExit(main())
