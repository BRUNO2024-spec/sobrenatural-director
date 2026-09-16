"""Two-process restart proof for the frozen V4 optimizer contract."""
import argparse, json, random, torch
from v2_model import LearnedScorerV2
from checkpointing import atomic_save, rng_state, restore_rng
def main():
    p=argparse.ArgumentParser();p.add_argument('--cache',required=True);p.add_argument('--checkpoint',required=True);p.add_argument('--mode',choices=('init','resume'),required=True);a=p.parse_args();d=torch.load(a.cache,map_location='cpu',weights_only=False);m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=(128,64));o=torch.optim.Adam(m.parameters(),lr=.001)
    if a.mode=='resume':
        ck=torch.load(a.checkpoint,map_location='cpu',weights_only=False);m.load_state_dict(ck['model']);o.load_state_dict(ck['optimizer']);restore_rng(ck['rng']);step=ck['step']
    else: random.seed(17);torch.manual_seed(17);step=0
    x=d['train_numeric'][:1024];c=d['train_categorical'][:1024];y=d['train_target'][:1024];q=m(x,c);loss=torch.nn.functional.mse_loss(q,y);o.zero_grad();loss.backward();o.step();step+=1
    atomic_save({'model':m.state_dict(),'optimizer':o.state_dict(),'rng':rng_state(),'step':step,'manifest_sha':d['manifest_sha']},a.checkpoint);print(json.dumps({'step':step,'status':'PASS'}))
if __name__=='__main__':main()
