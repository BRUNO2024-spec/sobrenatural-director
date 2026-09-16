"""Structural V4 audit: grouping, leakage, duplicates, and shard hashes."""
import argparse,hashlib,json
from pathlib import Path
from v4_dataset import iter_rows,SPLITS
def fp(r): return hashlib.sha256(json.dumps({'stateFeatures':r['stateFeatures'],'candidateFeatures':r['candidateFeatures']},sort_keys=True,separators=(',',':')).encode()).hexdigest()
def main():
 p=argparse.ArgumentParser();p.add_argument('root');p.add_argument('--out',required=True);a=p.parse_args();root=Path(a.root);seen=set();states={};counts={};bad=[];shards={}
 episodes={}
 for sp in SPLITS:
  rows=0; groups={}
  for r in iter_rows(root,sp):
   rows+=1; m=r['metadata']; sid=m['stateId']; states.setdefault(sid,sp); groups.setdefault(sid,[]).append(r); h=fp(r)
   if h in seen:bad.append('duplicate_input');seen.add(h)
   if any(k in r['stateFeatures'] or k in r['candidateFeatures'] for k in ('teacherQuality','teacherChosen','target','stateId','candidateId','seed','split','trainingAllowed','policyDecision')):bad.append('leakage')
   if m.get('episodeId'):episodes.setdefault(m['episodeId'],set()).add(sp)
  if any(len({x['metadata']['split'] for x in z})>1 for z in groups.values()):bad.append('cross_split_group')
  counts[sp]={'rows':rows,'states':len(groups)}
 for f in sorted(root.glob('*/part-*.jsonl')):shards[str(f.relative_to(root))]=hashlib.sha256(f.read_bytes()).hexdigest()
 bad.extend('episode_split' for z in episodes.values() if len(z)>1); result={'counts':counts,'total_states':len(states),'total_rows':sum(x['rows'] for x in counts.values()),'episodes':len(episodes),'duplicate_or_leakage_errors':bad,'shard_count':len(shards),'shards':shards,'status':'PASS' if not bad else 'FAIL'};json.dump(result,open(a.out,'w'),indent=2);print(json.dumps({k:result[k] for k in ('counts','total_states','total_rows','episodes','shard_count','status')}))
 if bad:raise SystemExit(1)
if __name__=='__main__':main()
