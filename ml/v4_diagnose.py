"""Postmortem statistics for consumed V3 blind data; no training or tuning."""
import argparse,collections,json,math,statistics
from pathlib import Path
from v3_dataset import iter_rows
NUM=['tension','pressure','fatigue','recoveryNeed','healthRatio','combatPower','isolation','underground','observingSite','safetyKnown','eventConcurrency','memoryPressure','cooldownActive','threadActive','threadAgeBucket','recentHigh','repetitionCount','episodeStep']
def rows(root,split): return list(iter_rows(root,split))
def flat(r):
 s=r['stateFeatures']; c=r['candidateFeatures']; return [float(s.get(k,0)) for k in NUM]+[c.get('candidateKind'),c.get('provider')]
def pct(v,q): return statistics.quantiles(v,n=100,method='inclusive')[max(0,min(99,int(q)-1))] if len(v)>1 else (v[0] if v else 0)
def summarize(rs):
 out={}
 for k in NUM:
  v=[float(r['stateFeatures'].get(k,0)) for r in rs];out[k]={'count':len(v),'min':min(v),'p01':pct(v,1),'p05':pct(v,5),'p25':pct(v,25),'median':statistics.median(v),'p75':pct(v,75),'p95':pct(v,95),'p99':pct(v,99),'max':max(v),'mean':statistics.mean(v),'std':statistics.pstdev(v)} if v else {}
 for k in ('family','environment','providerMode','narrativeState'):
  out[k]=dict(collections.Counter(r['stateFeatures'][k] for r in rs))
 for k in ('candidateKind','intent','provider'):
  out[k]=dict(collections.Counter(r['candidateFeatures'][k] for r in rs))
 return out
def state_groups(rs):
 g=collections.defaultdict(list)
 for r in rs:g[r['metadata']['stateId']].append(r)
 return g
def margins(rs):
 out=[]
 for z in state_groups(rs).values():
  q=sorted((float(r['supervision']['teacherQuality']) for r in z),reverse=True);out.append(q[0]-q[1] if len(q)>1 else 1)
 return out
def combos(rs):
 g=state_groups(rs);out={}
 for name,fn in {'environment_provider':lambda r:(r['stateFeatures']['environment'],r['stateFeatures']['providerMode']),'family_candidate':lambda r:(r['stateFeatures']['family'],r['candidateFeatures']['candidateKind']),'narrative_candidate':lambda r:(r['stateFeatures']['narrativeState'],r['candidateFeatures']['candidateKind']),'provider_candidate':lambda r:(r['stateFeatures']['providerMode'],r['candidateFeatures']['provider'])}.items():out[name]={"|".join(k):v for k,v in collections.Counter(fn(z[0]) for z in g.values()).items()}
 return out
def main():
 p=argparse.ArgumentParser();p.add_argument('--v3',required=True);p.add_argument('--blind',required=True);p.add_argument('--out',required=True);a=p.parse_args(); result={'v3_train':summarize(rows(a.v3,'TRAIN')),'v3_validation':summarize(rows(a.v3,'VALIDATION')),'blind_test':summarize(rows(a.blind,'TEST')),'blind_holdout':summarize(rows(a.blind,'HOLDOUT'))}
 allsets={'v3_train':rows(a.v3,'TRAIN'),'v3_validation':rows(a.v3,'VALIDATION'),'blind_test':rows(a.blind,'TEST'),'blind_holdout':rows(a.blind,'HOLDOUT')};
 for name,rs in allsets.items():
  mm=margins(rs); g=state_groups(rs); sizes=[len(x) for x in g.values()]; result[name+'_diagnostics']={'states':len(g),'mean_candidates':statistics.mean(sizes),'p95_candidates':pct(sizes,95),'margins':{'very_ambiguous':sum(x<=.02 for x in mm),'ambiguous':sum(.02<x<=.08 for x in mm),'moderate':sum(.08<x<=.20 for x in mm),'clear':sum(x>.20 for x in mm),'median':statistics.median(mm)},'combos':combos(rs)}
 json.dump(result,open(a.out,'w'),indent=2);print(json.dumps({k:v for k,v in result.items() if k.endswith('diagnostics')}))
if __name__=='__main__':main()
