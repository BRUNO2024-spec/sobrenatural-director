"""Frozen-model blind slices: family, environment, provider, narrative, sequence."""
import argparse,json,numpy as np,torch
from v2_model import LearnedScorerV2
from v4_preprocess import encode
from evaluate_v4_blind import iter_blind_rows,metrics
def main():
 p=argparse.ArgumentParser();p.add_argument('--cache',required=True);p.add_argument('--checkpoint',required=True);p.add_argument('--blind',required=True);p.add_argument('--out',required=True);a=p.parse_args();d=torch.load(a.cache,map_location='cpu',weights_only=False);ck=torch.load(a.checkpoint,map_location='cpu',weights_only=False);m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=tuple(ck['config']['hidden']));m.load_state_dict(ck['model_state_dict']);m.eval();prep=d['preprocessing'];out={}
 for split in ('TEST','HOLDOUT'):
  rec=[]
  for r in iter_blind_rows(a.blind,split):
   x,c,y=encode(r,prep);rec.append((x,c,y,r))
  xs=torch.tensor([x[0] for x in rec]);cs=torch.tensor([x[1] for x in rec]);pred=m(xs,cs).detach().numpy();y=np.array([x[2] for x in rec]);labels=[r for *_,r in rec];slices={}
  for field in ('family','environment','providerMode','narrativeState'):
   for val in sorted(set(r['stateFeatures'][field] for r in labels)):
    ix=[i for i,r in enumerate(labels) if r['stateFeatures'][field]==val];slices[field+'='+val]=metrics(pred[ix],y[ix],np.array([r['metadata']['stateId'] for r in labels])[ix])
  ix=[i for i,r in enumerate(labels) if r['metadata'].get('episodeId') is not None];slices['sequence']=metrics(pred[ix],y[ix],np.array([r['metadata']['stateId'] for r in labels])[ix]);out[split.lower()]={'slices':slices,'worst':min(((v['regret'],k,v) for k,v in slices.items()),key=lambda z:z[0])}
 json.dump(out,open(a.out,'w'),indent=2);print(json.dumps(out))
if __name__=='__main__':main()
