"""Post-freeze diagnostics: no fitting, tuning, or weight updates."""
import argparse, json, math, statistics
from pathlib import Path
import torch
from v2_model import LearnedScorerV2
from v2_evaluate import scores
from v3_dataset import iter_rows

def main():
    p=argparse.ArgumentParser(); p.add_argument('--cache',required=True); p.add_argument('--checkpoint',required=True); p.add_argument('--dataset',required=True); p.add_argument('--out',required=True); a=p.parse_args()
    d=torch.load(a.cache,map_location='cpu',weights_only=False); ck=torch.load(a.checkpoint,map_location='cpu',weights_only=False); c=ck['config']; hidden=tuple([c['hidden']]+[int(x) for x in c['layers'].split(',') if x]); m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=hidden,dropout=c['dropout'],activation=c['activation']); m.load_state_dict(ck['model_state_dict']); m.eval(); out={}
    for split in ('validation','test','holdout'):
        n,cat,y,g=d[split+'_numeric'],d[split+'_categorical'],d[split+'_target'],d[split+'_groups']
        with torch.no_grad(): pred=m(n,cat)
        out[split]=scores(pred,y,g)
        rows=list(iter_rows(a.dataset,split.upper())); labels=[]
        for r in rows: labels.append({'family':r['stateFeatures']['family'],'environment':r['stateFeatures']['environment'],'providerMode':r['stateFeatures']['providerMode'],'narrativeState':r['stateFeatures']['narrativeState'],'candidateKind':r['candidateFeatures']['candidateKind'],'episode':r['metadata']['episodeId'] is not None,'noAction':r['candidateFeatures']['candidateKind']=='no-action','stateId':r['metadata']['stateId'],'target':float(r['supervision']['teacherQuality'])})
        assert len(labels)==len(y)
        slices={}
        # Slice metrics are post-freeze diagnostics on held-out splits. The
        # validation pass is reserved for the cheaper feature-mask ablations.
        if split != 'validation':
            for field in ('family','environment','providerMode','narrativeState','candidateKind','episode'):
                for value in sorted(set(x[field] for x in labels),key=str):
                    ix=[i for i,x in enumerate(labels) if x[field]==value]; slices[field+'='+str(value)]={'size':len(ix),'metrics':scores(pred[ix],y[ix],g[ix])}
        out[split+'_slices']=slices
        # NO_ACTION is evaluated at state level; false positives mean selecting it
        # when the teacher best candidate is not NO_ACTION.
        by={}
        for i,x in enumerate(labels): by.setdefault(x['stateId'],[]).append(i)
        no_states=[ix for ix in by.values() if any(labels[i]['noAction'] for i in ix)]
        no_best=no_top=no_fp=no_fn=0; no_reg=[]
        for ix in no_states:
            ni=next(i for i in ix if labels[i]['noAction']); teacher=max(labels[i]['target'] for i in ix); ti=max(ix,key=lambda i:labels[i]['target']); pi=max(ix,key=lambda i:float(pred[i])); no_best+=labels[ti]['noAction']; no_top+=labels[pi]['noAction']; no_fp+=int(labels[pi]['noAction'] and not labels[ti]['noAction']); no_fn+=int(not labels[pi]['noAction'] and labels[ti]['noAction']); no_reg.append(teacher-labels[pi]['target'])
        out[split+'_no_action']={'state_count':len(no_states),'teacher_best_count':no_best,'top1':no_top/len(no_states) if no_states else 0,'false_positive_rate':no_fp/len(no_states) if no_states else 0,'false_negative_rate':no_fn/len(no_states) if no_states else 0,'mean_regret':sum(no_reg)/len(no_reg) if no_reg else 0}
        # sequence/static and near-boundary diagnostics
        for kind in (False,True):
            ix=[i for i,x in enumerate(labels) if x['episode']==kind]; out[split+('_sequence' if kind else '_static')]=scores(pred[ix],y[ix],g[ix]) if ix else {}
        by={};
        for i,x in enumerate(labels): by.setdefault(x['stateId'],[]).append(i)
        amb=[]
        for ix in by.values():
            q=sorted((labels[i]['target'] for i in ix),reverse=True)
            if len(q)>1 and q[0]-q[1]<=0.02: amb.extend(ix)
        out[split+'_ambiguous']=scores(pred[amb],y[amb],g[amb]) if amb else {'groups':0}
    # TRAIN/VALIDATION-only diagnostic input masks; frozen weights are never changed.
    n,cat,y,g=d['validation_numeric'],d['validation_categorical'],d['validation_target'],d['validation_groups']; base=out['validation']; ab={}
    masks={'numeric_only':(n,torch.zeros_like(cat)),'categorical_only':(torch.zeros_like(n),cat),'no_memory_repetition':(n.clone(),cat),'no_provider_context':(n.clone(),cat),'no_temporal_context':(n.clone(),cat)}
    for name,(nn,cc) in masks.items():
        if name=='no_memory_repetition': nn[:,[11,16]]=0
        if name=='no_provider_context': nn[:,[10]]=0; cc[:,[2,7]]=0
        if name=='no_temporal_context': nn[:,[13,14,15,17]]=0
        with torch.no_grad(): pp=m(nn,cc)
        mm=scores(pp,y,g); ab[name]={'validation':mm,'delta_pairwise':mm['pairwise']-base['pairwise'],'delta_regret':mm['mean_regret']-base['mean_regret']}
    out['ablations']=ab; json.dump(out,open(a.out,'w'),indent=2); print(json.dumps({'validation':out['validation'],'test':out['test'],'holdout':out['holdout'],'ablations':ab}))
if __name__=='__main__': main()
