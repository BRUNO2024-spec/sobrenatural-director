"""Fast post-freeze slice/no-action/temporal aggregation."""
import argparse,json
import numpy as np
import torch
from v2_model import LearnedScorerV2
from v3_dataset import iter_rows

def agg(p,y,g):
    groups={}
    for i,k in enumerate(g): groups.setdefault(int(k),[]).append(i)
    reg=[]; top=[]; pairs=good=0
    for ix in groups.values():
        best=max(y[ix]); chosen=max(ix,key=lambda i:p[i]); reg.append(best-y[chosen]); top.append(y[chosen]==best)
        for i in ix:
            for j in ix:
                if y[i]!=y[j]: pairs+=1; good+=(p[i]>p[j])==(y[i]>y[j])
    return {'size':len(p),'groups':len(groups),'pairwise':good/pairs if pairs else 0,'top1':float(np.mean(top)) if top else 0,'mean_regret':float(np.mean(reg)) if reg else 0}

def main():
    a=argparse.ArgumentParser(); a.add_argument('--cache',required=True); a.add_argument('--checkpoint',required=True); a.add_argument('--dataset',required=True); a.add_argument('--out',required=True); x=a.parse_args()
    torch.set_num_threads(4); d=torch.load(x.cache,map_location='cpu',weights_only=False); ck=torch.load(x.checkpoint,map_location='cpu',weights_only=False); c=ck['config']; m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=(c['hidden'],),dropout=c['dropout'],activation=c['activation']); m.load_state_dict(ck['model_state_dict']); m.eval(); out={}
    for split in ('test','holdout'):
        with torch.no_grad(): p=m(d[split+'_numeric'],d[split+'_categorical']).numpy()
        y=d[split+'_target'].numpy(); g=d[split+'_groups'].numpy(); rows=list(iter_rows(x.dataset,split.upper())); labels=[]
        for r in rows: labels.append({'family':r['stateFeatures']['family'],'environment':r['stateFeatures']['environment'],'providerMode':r['stateFeatures']['providerMode'],'narrativeState':r['stateFeatures']['narrativeState'],'candidateKind':r['candidateFeatures']['candidateKind'],'sequence':r['metadata']['episodeId'] is not None,'no_action':r['candidateFeatures']['candidateKind']=='no-action','state':r['metadata']['stateId']})
        s={}
        for field in ('family','environment','providerMode','narrativeState','candidateKind','sequence'):
            for value in sorted(set(str(z[field]) for z in labels)):
                ix=np.array([i for i,z in enumerate(labels) if str(z[field])==value]); s[field+'='+value]=agg(p[ix],y[ix],g[ix])
        out[split]={'overall':agg(p,y,g),'slices':s}; ix=np.array([i for i,z in enumerate(labels) if z['sequence']]); jx=np.array([i for i,z in enumerate(labels) if not z['sequence']]); out[split]['sequence']=agg(p[ix],y[ix],g[ix]) if len(ix) else {}; out[split]['static']=agg(p[jx],y[jx],g[jx]) if len(jx) else {}
        states={}
        for i,z in enumerate(labels): states.setdefault(z['state'],[]).append(i)
        no=[v for v in states.values() if any(labels[i]['no_action'] for i in v)]; fp=fn=hit=0; rr=[]
        for ix in no:
            teacher=max(ix,key=lambda i:y[i]); chosen=max(ix,key=lambda i:p[i]); tb=labels[teacher]['no_action']; cb=labels[chosen]['no_action']; hit+=cb==tb; fp+=cb and not tb; fn+=tb and not cb; rr.append(y[teacher]-y[chosen])
        out[split]['no_action']={'state_count':len(no),'top1':hit/len(no) if no else 0,'false_positive_rate':fp/len(no) if no else 0,'false_negative_rate':fn/len(no) if no else 0,'mean_regret':float(np.mean(rr)) if rr else 0}
    json.dump(out,open(x.out,'w'),indent=2); print(json.dumps(out))
if __name__=='__main__': main()
