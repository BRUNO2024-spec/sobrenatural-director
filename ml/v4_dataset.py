"""V4 reader with strict development-split and leakage guards."""
import json
from pathlib import Path
SCHEMA='DIRECTOR_EXPERIENCE_FEATURES_V4';SPLITS=('TRAIN','VALIDATION_IID','VALIDATION_STRESS')
def iter_rows(root,split):
 if split not in SPLITS:raise ValueError('invalid V4 split')
 for p in sorted((Path(root)/split).glob('part-*.jsonl')):
  for line in p.read_text(encoding='utf8').splitlines():
   r=json.loads(line);m=r.get('metadata',{});inp={**r.get('stateFeatures',{}),**r.get('candidateFeatures',{})}
   if m.get('schemaVersion')!=SCHEMA or m.get('split')!=split or m.get('trainingAllowed')!=(split=='TRAIN'):raise ValueError('schema/split policy')
   if any(k in inp for k in ('teacherQuality','teacherChosen','target','policyDecision','stateId','candidateId','seed','split','trainingAllowed','curriculumBucket')):raise ValueError('leakage')
   yield r
