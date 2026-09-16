"""TRAIN-only streaming preprocessing for V3 consumption smoke/future training."""
import math
try:
    from .v3_dataset import iter_rows
except ImportError:
    from v3_dataset import iter_rows
NUMERIC=["tension","pressure","fatigue","recoveryNeed","healthRatio","combatPower","isolation","underground","observingSite","safetyKnown","eventConcurrency","memoryPressure","cooldownActive","threadActive","threadAgeBucket","recentHigh","repetitionCount","episodeStep","baseUtility"]
CATEGORICAL=["family","environment","providerMode","narrativeState","candidateKind","intent","safety","provider"]
def value(r,k):
    src=r["candidateFeatures"] if k=="baseUtility" else r["stateFeatures"]
    return float(src.get(k,False))
def cat(r,k):
    src=r["candidateFeatures"] if k in CATEGORICAL[4:] else r["stateFeatures"]
    return str(src.get(k,"<UNK>"))
def fit(root):
    vals={k:[] for k in NUMERIC}; vocab={k:set() for k in CATEGORICAL}; count=0
    for r in iter_rows(root,"TRAIN"):
        count+=1
        for k in NUMERIC: vals[k].append(value(r,k))
        for k in CATEGORICAL: vocab[k].add(cat(r,k))
    if not count: raise ValueError("empty TRAIN")
    prep={"numeric":NUMERIC,"categorical":CATEGORICAL,"means":{},"stds":{},"vocab":{}}
    for k,x in vals.items():
        m=sum(x)/len(x); prep["means"][k]=m; prep["stds"][k]=max(math.sqrt(sum((z-m)**2 for z in x)/len(x)),1e-12)
    for k,x in vocab.items(): prep["vocab"][k]=["<UNK>"]+sorted(x)
    return prep
def encode(r,prep):
    x=[(value(r,k)-prep["means"][k])/prep["stds"][k] for k in prep["numeric"]]
    c=[prep["vocab"][k].index(cat(r,k)) if cat(r,k) in prep["vocab"][k] else 0 for k in prep["categorical"]]
    return x,c,float(r["supervision"]["teacherQuality"])
