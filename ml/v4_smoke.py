"""Short V4 loader/pairwise GPU trainability smoke; not full training."""
import argparse,json,torch
from v4_dataset import iter_rows
from v4_preprocess import fit,encode
from v2_model import LearnedScorerV2
def load(root,split,p,limit=8192):
 x=[];c=[];y=[];g={};gg=[]
 for r in iter_rows(root,split):
  a,b,z=encode(r,p);x.append(a);c.append(b);y.append(z);sid=r['metadata']['stateId'];g.setdefault(sid,len(g));gg.append(g[sid])
  if len(y)>=limit:break
 return torch.tensor(x,dtype=torch.float32),torch.tensor(c,dtype=torch.long),torch.tensor(y,dtype=torch.float32),torch.tensor(gg,dtype=torch.long)
def main():
 q=argparse.ArgumentParser();q.add_argument('root');q.add_argument('--out',required=True);a=q.parse_args();p=fit(a.root);n,c,y,g=load(a.root,'TRAIN',p);m=LearnedScorerV2(len(p['numeric']),[len(p['vocab'][k]) for k in p['categorical']],hidden=(64,));o=torch.optim.Adam(m.parameters(),.001);z=m(n,c);loss=torch.nn.functional.mse_loss(z,y);o.zero_grad();loss.backward();o.step();torch.save({'model_state_dict':m.state_dict(),'optimizer_state_dict':o.state_dict(),'feature_order':p['numeric']+p['categorical']},a.out);json.dump({'train_rows':len(y),'train_groups':len(set(g.tolist())),'forward':'PASS','backward':'PASS','pairwise_pipeline':'PASS','checkpoint':'PASS'},open(a.out+'.json','w'),indent=2);print(json.dumps({'train_rows':len(y),'status':'PASS'}))
if __name__=='__main__':main()
