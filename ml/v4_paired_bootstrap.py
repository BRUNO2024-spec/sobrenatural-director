"""Paired state bootstrap between frozen selected and small V4 models."""
import argparse,json,random,numpy as np,torch
from v4_preprocess import encode
from evaluate_v4_blind import iter_blind_rows
from v2_model import LearnedScorerV2
def main():
 p=argparse.ArgumentParser();p.add_argument('--cache',required=True);p.add_argument('--selected',required=True);p.add_argument('--small',required=True);p.add_argument('--blind',required=True);p.add_argument('--out',required=True);a=p.parse_args();d=torch.load(a.cache,map_location='cpu',weights_only=False);prep=d['preprocessing'];models=[]
 for path in (a.selected,a.small):
  ck=torch.load(path,map_location='cpu',weights_only=False);m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=tuple(ck['config']['hidden']));m.load_state_dict(ck['model_state_dict']);m.eval();models.append(m)
 out={}
 for split in ('TEST','HOLDOUT'):
  rows=[];groups={}
  for r in iter_blind_rows(a.blind,split):
   x,c,y=encode(r,prep);sid=r['metadata']['stateId'];groups.setdefault(sid,[]).append((x,c,y))
  regrets=[[],[]];tops=[[],[]]
  for z in groups.values():
   xs=torch.tensor([q[0] for q in z]);cs=torch.tensor([q[1] for q in z]);ys=np.array([q[2] for q in z]);pred=[m(xs,cs).detach().numpy() for m in models];best=ys.max()
   for j in range(2):regrets[j].append(best-ys[pred[j].argmax()]);tops[j].append(float(ys[pred[j].argmax()]==best))
  rng=np.random.default_rng(17);dreg=np.array(regrets[0])-np.array(regrets[1]);dtop=np.array(tops[0])-np.array(tops[1]);boot=[]
  for _ in range(1000):ix=rng.integers(0,len(dreg),len(dreg));boot.append((dreg[ix].mean(),dtop[ix].mean()))
  out[split.lower()]={'selected_minus_small_regret':float(dreg.mean()),'selected_minus_small_top1':float(dtop.mean()),'regret_ci95':[float(np.percentile([x[0] for x in boot],2.5)),float(np.percentile([x[0] for x in boot],97.5))],'top1_ci95':[float(np.percentile([x[1] for x in boot],2.5)),float(np.percentile([x[1] for x in boot],97.5))]}
 json.dump(out,open(a.out,'w'),indent=2);print(json.dumps(out))
if __name__=='__main__':main()
