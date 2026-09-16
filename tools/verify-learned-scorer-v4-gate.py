from pathlib import Path
import sys

def main():
    data = dict(line.split('=', 1) for line in Path(sys.argv[1]).read_text().splitlines() if line and not line.startswith('#'))
    expected = {'V3_BLIND_STATUS':'CONSUMED_DIAGNOSTIC_DATA','GENERALIZATION_GAP_REPRODUCED':'PASS','CURRICULUM_FREEZE':'PASS','GENERATION_SPEC_FREEZE':'PASS','FUTURE_BLIND_SPEC_FREEZE':'PASS','FUTURE_BLIND_DATASET_GENERATED':'NO','FUTURE_BLIND_OPENED':'NO','PILOT_SMALL':'PASS','PILOT_MEDIUM':'PASS','TOTAL_STATE_GROUPS':'120000','HISTORICAL_V3_INPUT_OVERLAP':'0','CROSS_SPLIT_STATE_INPUT_DUPLICATES':'0','CROSS_SPLIT_CANDIDATE_INPUT_DUPLICATES':'0','TARGET_NOT_IN_INPUTS':'PASS','POST_DECISION_INPUTS':'0','OOM_COUNT':'0','FULL_V4_TRAINING_PERFORMED':'NO','SHADOW_MODE_ENABLED':'NO','PRODUCTION_RUNTIME_BEHAVIOR_CHANGED':'NO','HARD_SAFETY_CHANGED':'NO','PROTECTED_WORLD_TOUCHED':'NO','ORIGINAL_MODS_MODIFIED':'NO','SECRETS_COMMITTED':'NO','RELEASE_FINAL_DECLARED':'NO','FINAL_GATE':'PASS'}
    bad = [f'{k}={data.get(k)} expected {v}' for k, v in expected.items() if data.get(k) != v]
    print('gate PASS' if not bad else '\n'.join(bad))
    return 0 if not bad else 1

if __name__ == '__main__':
    raise SystemExit(main())
