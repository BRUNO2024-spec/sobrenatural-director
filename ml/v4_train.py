"""Compact V4 development training; never reads future blind data."""
import argparse,json,random,time
from pathlib import Path
import torch
from v2_model import LearnedScorerV2
from v2_closure_fast import agg
from checkpointing import atomic_save, rng_state

def pairs(y,g):
    by={}
    for j in range(len(y)): by.setdefault(int(g[j]),[]).append(j)
    return [(max(z,key=lambda j:float(y[j])),j) for z in by.values() for j in z if float(y[max(z,key=lambda k:float(y[k]))])-float(y[j])>1e-9]
def run(cache,out,hidden,objective,seed,epochs,lr):
    random.seed(seed);torch.manual_seed(seed);d=torch.load(cache,map_location='cpu',weights_only=False);dev=torch.device('cuda' if torch.cuda.is_available() else 'cpu');m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=hidden,dropout=0).to(dev);opt=torch.optim.Adam(m.parameters(),lr=lr);po=torch.tensor(pairs(d['train_target'],d['train_groups']),dtype=torch.long);hist=[];start=time.time()
    for ep in range(epochs):
        order=torch.randperm(len(po),generator=torch.Generator().manual_seed(seed+ep));loss_sum=0
        for ix in po[order].split(32768):
            a,b=ix[:,0],ix[:,1];qa=m(d['train_numeric'][a].to(dev),d['train_categorical'][a].to(dev));qb=m(d['train_numeric'][b].to(dev),d['train_categorical'][b].to(dev));loss=torch.relu(.02-qa+qb).mean()
            if objective=='hybrid': loss=loss+.25*torch.nn.functional.mse_loss(qa,d['train_target'][a].to(dev))+.25*torch.nn.functional.mse_loss(qb,d['train_target'][b].to(dev))
            opt.zero_grad();loss.backward();opt.step();loss_sum+=float(loss)
        m.eval();metrics={}
        with torch.no_grad():
            for split in ('validation_iid','validation_stress'):
                pred=m(d[split+'_numeric'].to(dev),d[split+'_categorical'].to(dev)).cpu().numpy();metrics[split]=agg(pred,d[split+'_target'].numpy(),d[split+'_groups'].numpy())
        hist.append({'epoch':ep+1,'metrics':metrics,'loss':loss_sum})
    out=Path(out);out.mkdir(parents=True,exist_ok=True);m.cpu();atomic_save({'model_state_dict':m.state_dict(),'optimizer_state_dict':opt.state_dict(),'scheduler_state_dict':None,'rng_state':rng_state(),'config':{'hidden':hidden,'objective':objective,'seed':seed,'epochs':epochs,'lr':lr},'manifest_sha':d['manifest_sha'],'epoch':epochs,'global_step':len(po)//32768*epochs},out/'best.pt');json.dump({'parameters':sum(x.numel() for x in m.parameters()),'history':hist,'elapsed':time.time()-start,'best':min(hist,key=lambda x:.6*x['metrics']['validation_iid']['mean_regret']+.4*x['metrics']['validation_stress']['mean_regret'])},open(out/'result.json','w'),indent=2);print(json.dumps({'parameters':sum(x.numel() for x in m.parameters()),'best':hist[-1]}))
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--cache',required=True);p.add_argument('--out',required=True);p.add_argument('--hidden',type=int,nargs='+',default=[64]);p.add_argument('--objective',choices=('pairwise','hybrid'),default='pairwise');p.add_argument('--seed',type=int,default=7);p.add_argument('--epochs',type=int,default=4);p.add_argument('--lr',type=float,default=1e-3);a=p.parse_args();run(a.cache,a.out,tuple(a.hidden),a.objective,a.seed,a.epochs,a.lr)
