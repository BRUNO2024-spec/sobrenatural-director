"""Historical input/candidate overlap audit for fresh V4 rows."""
import argparse,hashlib,json
from pathlib import Path
def rows(root,splits):
 for sp in splits:
  for p in sorted((Path(root)/sp).glob('part-*.jsonl')):
   for line in p.read_text().splitlines():yield json.loads(line)
def fp(r):return hashlib.sha256(json.dumps({'stateFeatures':r['stateFeatures'],'candidateFeatures':r['candidateFeatures']},sort_keys=True,separators=(',',':')).encode()).hexdigest()
def main():
 p=argparse.ArgumentParser();p.add_argument('--v4',required=True);p.add_argument('--v3',required=True);p.add_argument('--blind',required=True);p.add_argument('--out',required=True);a=p.parse_args();v4f=set();v4c=set();historical=set();historicalc=set()
 for r in rows(a.v4,('TRAIN','VALIDATION_IID','VALIDATION_STRESS')):v4f.add(fp(r));v4c.add(r['metadata']['candidateId'])
 for root,splits in ((a.v3,('TRAIN','VALIDATION','TEST','HOLDOUT')),(a.blind,('TEST','HOLDOUT'))):
  for r in rows(root,splits):historical.add(fp(r));historicalc.add(r['metadata']['candidateId'])
 out={'v4_states_input_fingerprints':len(v4f),'historical_input_overlap':len(v4f&historical),'historical_candidate_overlap':len(v4c&historicalc),'status':'PASS' if not (v4f&historical or v4c&historicalc) else 'FAIL'};json.dump(out,open(a.out,'w'),indent=2);print(json.dumps(out));raise SystemExit(0 if out['status']=='PASS' else 1)
if __name__=='__main__':main()
