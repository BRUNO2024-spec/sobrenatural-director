"""Two-process checkpoint restart proof using the frozen architecture/config."""
import argparse, json, random
import torch
from v2_model import LearnedScorerV2
from checkpointing import atomic_save, rng_state, restore_rng
def model_for(d, ck):
    c=ck['config']; h=tuple([c['hidden']]+[int(x) for x in c['layers'].split(',') if x]); return LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=h,dropout=c['dropout'],activation=c['activation'])
def main():
    p=argparse.ArgumentParser(); p.add_argument('--mode',choices=['init','resume'],required=True); p.add_argument('--cache',required=True); p.add_argument('--base',required=True); p.add_argument('--checkpoint',required=True); p.add_argument('--proof',required=True); a=p.parse_args(); d=torch.load(a.cache,map_location='cpu',weights_only=False)
    if a.mode=='init':
        ck=torch.load(a.base,map_location='cpu',weights_only=False); random.seed(701); torch.manual_seed(701); m=model_for(d,ck); o=torch.optim.Adam(m.parameters(),lr=ck['config']['lr'],weight_decay=ck['config']['wd']); state=rng_state(); expected={'python':repr(random.getstate()),'torch':torch.get_rng_state().tolist()};
        if torch.cuda.is_available(): expected['cuda']=torch.cuda.get_rng_state_all()[0].tolist()
        ck.update({'model_state_dict':m.state_dict(),'optimizer_state_dict':o.state_dict(),'global_step':int(ck.get('global_step',0))+1,'epoch':int(ck.get('epoch',0)),'rng_state':state,'scheduler_state_dict':None,'scaler_state_dict':None,'resume_expected':expected,'checkpoint_schema':'V2_CLOSURE_CHECKPOINT'}); atomic_save(ck,a.checkpoint); json.dump({'pre_restart_global_step':ck['global_step'],'expected':expected},open(a.proof,'w'),indent=2); print(json.dumps({'pre_restart_global_step':ck['global_step']})); return
    ck=torch.load(a.checkpoint,map_location='cpu',weights_only=False); m=model_for(d,ck); m.load_state_dict(ck['model_state_dict']); o=torch.optim.Adam(m.parameters(),lr=ck['config']['lr'],weight_decay=ck['config']['wd']); o.load_state_dict(ck['optimizer_state_dict']); restore_rng(ck['rng_state']); actual={'python':repr(random.getstate()),'torch':torch.get_rng_state().tolist()};
    if torch.cuda.is_available(): actual['cuda']=torch.cuda.get_rng_state_all()[0].tolist()
    exp=ck['resume_expected']; rng_ok=actual==exp; n,c,y=d['train_numeric'][:1024],d['train_categorical'][:1024],d['train_target'][:1024]; loss=torch.nn.functional.mse_loss(m(n,c),y); o.zero_grad(); loss.backward(); o.step(); ck.update({'model_state_dict':m.state_dict(),'optimizer_state_dict':o.state_dict(),'global_step':int(ck['global_step'])+1,'rng_restore_verified':rng_ok}); atomic_save(ck,a.checkpoint); result={'restored_global_step':ck['global_step']-1,'post_resume_global_step':ck['global_step'],'optimizer_state_restored':True,'scheduler_state_restored':'NOT_APPLICABLE','scaler_state_restored':'NOT_APPLICABLE','python_rng_restored':rng_ok,'torch_cpu_rng_restored':rng_ok,'torch_cuda_rng_restored':rng_ok if torch.cuda.is_available() else 'NOT_APPLICABLE','hash_guards':all(k in ck for k in ('cache_sha','feature_schema','preprocessing'))}; json.dump(result,open(a.proof,'w'),indent=2); print(json.dumps(result))
if __name__=='__main__': main()
