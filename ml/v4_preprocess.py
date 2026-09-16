"""TRAIN-only preprocessing for V4 development splits."""
import math
from v4_dataset import iter_rows
NUMERIC=['tension','pressure','fatigue','recoveryNeed','healthRatio','combatPower','isolation','underground','observingSite','safetyKnown','eventConcurrency','memoryPressure','cooldownActive','threadActive','threadAgeBucket','recentHigh','repetitionCount','episodeStep','baseUtility']
CATEGORICAL=['family','environment','providerMode','narrativeState','candidateKind','intent','safety','provider']
def fit(root):
 vals={k:[] for k in NUMERIC};v={k:set() for k in CATEGORICAL}
 for r in iter_rows(root,'TRAIN'):
  for k in NUMERIC:vals[k].append(float(r['candidateFeatures'].get(k,r['stateFeatures'].get(k,False))))
  for k in CATEGORICAL:v[k].add(str(r['candidateFeatures'].get(k,r['stateFeatures'].get(k,'<UNK>'))))
 out={'numeric':NUMERIC,'categorical':CATEGORICAL,'means':{},'stds':{},'vocab':{}}
 for k,x in vals.items():m=sum(x)/len(x);out['means'][k]=m;out['stds'][k]=max(math.sqrt(sum((z-m)**2 for z in x)/len(x)),1e-12)
 for k,x in v.items():out['vocab'][k]=['<UNK>']+sorted(x)
 return out
def encode(r,p):
 x=[(float(r['candidateFeatures'].get(k,r['stateFeatures'].get(k,False)))-p['means'][k])/p['stds'][k] for k in p['numeric']];c=[]
 for k in p['categorical']:
  z=str(r['candidateFeatures'].get(k,r['stateFeatures'].get(k,'<UNK>')));c.append(p['vocab'][k].index(z) if z in p['vocab'][k] else 0)
 return x,c,float(r['supervision']['teacherQuality'])
