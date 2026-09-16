"""Frozen V4 blind evaluator; no optimizer updates or tuning."""
import argparse,json,math
from pathlib import Path
import numpy as np, torch
from v2_model import LearnedScorerV2
from v4_preprocess import encode

def iter_blind_rows(root, split):
    if split not in ('TEST','HOLDOUT'): raise ValueError('invalid blind split')
    for path in sorted((Path(root)/split).glob('part-*.jsonl')):
        for line in path.read_text(encoding='utf8').splitlines(): yield json.loads(line)

def metrics(pred,y,groups):
    reg=[];top=[];rr=[];nd=[];pairs=good=0; by={}
    for i,g in enumerate(groups):by.setdefault(g,[]).append(i)
    for ix in by.values():
        order=sorted(ix,key=lambda i:pred[i],reverse=True); truth=sorted(ix,key=lambda i:y[i],reverse=True); best=max(y[i] for i in ix); rank=order.index(max(ix,key=lambda i:y[i]))+1
        reg.append(best-y[order[0]]);top.append(rank==1);rr.append(1/rank);dcg=sum((2**y[i]-1)/math.log2(j+2) for j,i in enumerate(order));idcg=sum((2**y[i]-1)/math.log2(j+2) for j,i in enumerate(truth));nd.append(dcg/idcg if idcg else 1.0)
        for i in ix:
            for j in ix:
                if y[i]!=y[j]:pairs+=1;good+=(pred[i]>pred[j])==(y[i]>y[j])
    pc=np.corrcoef(pred,y)[0,1] if np.std(pred) and np.std(y) else 0
    ranks=lambda z: np.argsort(np.argsort(z))
    sp=np.corrcoef(ranks(pred),ranks(y))[0,1]
    return {'rows':len(y),'states':len(by),'mse':float(np.mean((pred-y)**2)),'rmse':float(np.sqrt(np.mean((pred-y)**2))),'mae':float(np.mean(np.abs(pred-y))),'pearson':float(pc),'spearman':float(sp),'pairwise':good/pairs if pairs else 0,'top1':float(np.mean(top)),'mrr':float(np.mean(rr)),'ndcg':float(np.mean(nd)),'regret':float(np.mean(reg))}
def main():
    p=argparse.ArgumentParser();p.add_argument('--cache',required=True);p.add_argument('--checkpoint',required=True);p.add_argument('--blind',required=True);p.add_argument('--out',required=True);a=p.parse_args();d=torch.load(a.cache,map_location='cpu',weights_only=False);ck=torch.load(a.checkpoint,map_location='cpu',weights_only=False);m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=tuple(ck['config']['hidden']),dropout=0);m.load_state_dict(ck['model_state_dict']);m.eval();prep=d['preprocessing'];out={}
    for split in ('TEST','HOLDOUT'):
        xs=[];cs=[];ys=[];gs=[];groups={}
        for r in iter_blind_rows(a.blind,split):
            x,c,y=encode(r,prep);xs.append(x);cs.append(c);ys.append(y);sid=r['metadata']['stateId'];groups.setdefault(sid,len(groups));gs.append(groups[sid])
        with torch.no_grad(): pred=m(torch.tensor(xs),torch.tensor(cs)).numpy()
        out[split.lower()]=metrics(pred,np.asarray(ys),np.asarray(gs))
    json.dump(out,open(a.out,'w'),indent=2);print(json.dumps(out))
if __name__=='__main__':main()
