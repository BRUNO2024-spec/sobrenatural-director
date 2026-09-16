"""Locked-split evaluation for the offline V2 study."""
import argparse, json, math
import torch
from v2_model import LearnedScorerV2

def scores(pred, target, groups):
    pred, target = pred.tolist(), target.tolist(); err=[a-b for a,b in zip(pred,target)]
    mse=sum(x*x for x in err)/len(err); groups_out={}
    for i,g in enumerate(groups.tolist()): groups_out.setdefault(g,[]).append((pred[i],target[i]))
    regrets=[]; top=0; pairs=ok=0; mrr=ndcg=0.0
    for vals in groups_out.values():
        best=max(v[1] for v in vals); pi=max(range(len(vals)),key=lambda i:vals[i][0]); regrets.append(best-vals[pi][1]); top+=vals[pi][1]==best
        order=sorted(range(len(vals)),key=lambda i:vals[i][0],reverse=True); ideal=sorted((v[1] for v in vals),reverse=True)
        dcg=sum((vals[i][1]/math.log2(j+2)) for j,i in enumerate(order)); idcg=sum((v/math.log2(j+2)) for j,v in enumerate(ideal)); ndcg+=dcg/idcg if idcg else 1
        rank=1+next(j for j,i in enumerate(order) if vals[i][1]==best); mrr+=1/rank
        for a in vals:
            for b in vals:
                if a[1]==b[1]: continue
                pairs+=1; ok += (a[0]>b[0])==(a[1]>b[1])
    ma=sum(abs(x) for x in err)/len(err); pm=sum((a-sum(pred)/len(pred))*(b-sum(target)/len(target)) for a,b in zip(pred,target)); den1=sum((a-sum(pred)/len(pred))**2 for a in pred)**.5; den2=sum((b-sum(target)/len(target))**2 for b in target)**.5
    return {"mse":mse,"rmse":mse**.5,"mae":ma,"pearson":pm/(den1*den2) if den1 and den2 else 0,"pairwise":ok/pairs if pairs else 0,"top1":top/len(groups_out),"mrr":mrr/len(groups_out),"ndcg":ndcg/len(groups_out),"mean_regret":sum(regrets)/len(regrets),"groups":len(groups_out)}
def main():
    p=argparse.ArgumentParser(); p.add_argument("--cache",required=True); p.add_argument("--checkpoint",required=True); p.add_argument("--split",choices=["train","validation","test","holdout"],required=True); p.add_argument("--out",required=True); a=p.parse_args(); d=torch.load(a.cache,map_location="cpu",weights_only=False); ck=torch.load(a.checkpoint,map_location="cpu",weights_only=False); c=ck["config"]; hidden=tuple([c["hidden"]]+[int(x) for x in c["layers"].split(",") if x]); model=LearnedScorerV2(d["numeric_dim"],d["vocab_sizes"],hidden=hidden,dropout=c["dropout"],activation=c["activation"]); model.load_state_dict(ck["model_state_dict"]); s=a.split; n,cat,y,g=d[s+"_numeric"],d[s+"_categorical"],d[s+"_target"],d[s+"_groups"]
    with torch.no_grad(): pred=model(n,cat)
    result={"split":s,"manifest_sha":d["manifest_sha"],"metrics":scores(pred,y,g)}; json.dump(result,open(a.out,"w"),indent=2); print(json.dumps(result))
if __name__=="__main__": main()
