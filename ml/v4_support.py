"""Sampled input-only nearest-support diagnostic; no target is consumed."""
import argparse,json,random
import numpy as np
NUM=['tension','pressure','fatigue','recoveryNeed','healthRatio','combatPower','isolation','underground','observingSite','safetyKnown','eventConcurrency','memoryPressure','cooldownActive','threadActive','threadAgeBucket','recentHigh','repetitionCount','episodeStep']
CAT=['family','environment','providerMode','narrativeState','candidateKind','intent','safety','provider']
def iter_any(root,split):
 for p in __import__('pathlib').Path(root,split).glob('part-*.jsonl'):
  for line in p.read_text().splitlines():yield __import__('json').loads(line)
def states(root,split):
 out={}
 for r in iter_any(root,split):out.setdefault(r['metadata']['stateId'],r)
 return list(out.values())
def matrix(rs):
 return np.array([[float(r['stateFeatures'].get(k,0)) for k in NUM]+[float(r['candidateFeatures'].get('baseUtility',0))/100.0] for r in rs],dtype=np.float32),[[str(r['stateFeatures'].get(k,r['candidateFeatures'].get(k,''))) for k in CAT] for r in rs]
def main():
 p=argparse.ArgumentParser();p.add_argument('--train',required=True);p.add_argument('--validation',required=True);p.add_argument('--blind',required=True);p.add_argument('--v4train',default='');p.add_argument('--out',required=True);a=p.parse_args();tr=states(a.train,'TRAIN');va=states(a.validation,'VALIDATION');bl=states(a.blind,'TEST');tn,tc=matrix(tr);mean=tn.mean(0);std=np.maximum(tn.std(0),1e-9);tn=(tn-mean)/std;rng=random.Random(17)
 def dist(rs):
  rs=rs if len(rs)<=300 else rng.sample(rs,300);xn,xc=matrix(rs);xn=(xn-mean)/std;vals=[]
  for i in range(0,len(xn),50):
   d=((xn[i:i+50,None,:]-tn[None,:,:])**2).mean(2)**.5
   for j in range(i,min(i+50,len(xn))):vals.append(float(np.min(d[j-i]+np.array([sum(a!=b for a,b in zip(xc[j],z)) for z in tc],dtype=np.float32)*0.25)))
  return {'count':len(vals),'p50':float(np.percentile(vals,50)),'p90':float(np.percentile(vals,90)),'p95':float(np.percentile(vals,95)),'p99':float(np.percentile(vals,99))}
 out={'validation_to_train':dist(va),'blind_to_v3_train':dist(bl)}
 if a.v4train:
  v4n,v4c=matrix(states(a.v4train,'TRAIN'));v4mean=v4n.mean(0);v4std=np.maximum(v4n.std(0),1e-9);v4n=(v4n-v4mean)/v4std;oldtn,oldtc,oldmean,oldstd=tn,tc,mean,std;tn,tc,mean,std=v4n,v4c,v4mean,v4std;out['blind_to_v4_train']=dist(bl);tn,tc,mean,std=oldtn,oldtc,oldmean,oldstd
 json.dump(out,open(a.out,'w'),indent=2);print(json.dumps(out))
if __name__=='__main__':main()
