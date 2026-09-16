"""TRAIN/VALIDATION-only V3 cross-family experiment runner."""
import argparse,json,random,time
from pathlib import Path
import torch
from v3_model import ExplicitCrossScorer,CrossNetworkScorer
from v2_model import LearnedScorerV2
from v2_evaluate import scores
def main():
 p=argparse.ArgumentParser(); p.add_argument('--cache',required=True);p.add_argument('--out',required=True);p.add_argument('--family',choices=['mlp','explicit','cross'],required=True);p.add_argument('--depth',type=int,default=1);p.add_argument('--seed',type=int,default=7);p.add_argument('--epochs',type=int,default=4);p.add_argument('--lr',type=float,default=1e-3);p.add_argument('--run-id',required=True);a=p.parse_args(); random.seed(a.seed);torch.manual_seed(a.seed);d=torch.load(a.cache,map_location='cpu',weights_only=False); device=torch.device('cuda' if torch.cuda.is_available() else 'cpu')
 if a.family=='mlp':m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=(64,),dropout=0.0).to(device)
 elif a.family=='explicit':m=ExplicitCrossScorer(d['numeric_dim'],d['vocab_sizes'],hidden=64).to(device)
 else:m=CrossNetworkScorer(d['numeric_dim'],d['vocab_sizes'],hidden=64,depth=a.depth).to(device)
 o=torch.optim.Adam(m.parameters(),lr=a.lr);n,c,y,g=d['train_numeric'],d['train_categorical'],d['train_target'],d['train_groups'];vn,vc,vy,vg=d['validation_numeric'],d['validation_categorical'],d['validation_target'],d['validation_groups']; pairs=[];by={}
 for j in range(len(y)):by.setdefault(int(g[j]),[]).append(j)
 for z in by.values(): w=max(z,key=lambda j:float(y[j]));pairs.extend((w,j) for j in z if float(y[w])-float(y[j])>1e-9)
 po=torch.tensor(pairs,dtype=torch.long); hist=[];start=time.time()
 for epoch in range(a.epochs):
  m.train(); order=torch.randperm(len(po),generator=torch.Generator().manual_seed(a.seed+epoch)); total=0
  for ix in po[order].split(32768):
   q=m(n[ix[:,0]].to(device),c[ix[:,0]].to(device));r=m(n[ix[:,1]].to(device),c[ix[:,1]].to(device));loss=torch.relu(0.02-q+r).mean();o.zero_grad();loss.backward();o.step();total+=float(loss); 
  m.eval();
  with torch.no_grad(): pred=m(vn.to(device),vc.to(device)).cpu()
  mm=scores(pred,vy,vg);hist.append({'epoch':epoch+1,'validation':mm,'train_loss':total});print(json.dumps(hist[-1]),flush=True)
 out=Path(a.out);out.mkdir(parents=True,exist_ok=True);torch.save({'model_state_dict':m.state_dict(),'config':vars(a),'best_metric':hist[-1]['validation']['mean_regret'],'epoch':a.epochs,'global_step':len(po)//32768*a.epochs,'manifest_sha':d['manifest_sha']},out/'best.pt');json.dump({'runId':a.run_id,'family':a.family,'depth':a.depth,'parameters':sum(x.numel() for x in m.parameters()),'history':hist,'best':min((x['validation'] for x in hist),key=lambda x:x['mean_regret']),'elapsed':time.time()-start},open(out/'result.json','w'),indent=2)
if __name__=='__main__':main()
