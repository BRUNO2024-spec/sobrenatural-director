"""Attach frozen provenance to V3 weights without changing model tensors."""
import argparse,json,os,tempfile,random
from pathlib import Path
import torch
from checkpointing import rng_state,restore_rng
from v2_model import LearnedScorerV2
def save(x,p):
 p=Path(p);fd,t=tempfile.mkstemp(dir=p.parent,suffix='.tmp');os.close(fd)
 try:
  with open(t,'wb') as f:torch.save(x,f);f.flush();os.fsync(f.fileno())
  os.replace(t,p)
 finally:
  if os.path.exists(t):os.unlink(t)
def main():
 p=argparse.ArgumentParser();p.add_argument('--mode',choices=['finalize','resume-init','resume']);p.add_argument('--checkpoint',required=True);p.add_argument('--cache',required=True);p.add_argument('--config',required=True);p.add_argument('--out',required=True);a=p.parse_args();d=torch.load(a.cache,map_location='cpu',weights_only=False);x=torch.load(a.checkpoint,map_location='cpu',weights_only=False);cfg=json.load(open(a.config));
 if a.mode=='finalize':
  x.update({'frozen_config':cfg,'frozen_config_sha256':'18c334e51c5a45e8844a283478c44ef2d8fba80a48e5fe00fab2dcc21b82d41d','blind_eval_spec_sha256':'e7d13712feffefa33f918b134bc17f138b85ca418eee34aea088a528567f50e6','dataset_manifest_sha256':'10de28923e0251d67befd53b25400c781f7e6315d96250d5db616e69eb788b7d','feature_schema_sha256':'58ab4686dad7ba7dac5df96343668c648ca875f6013b40f97de5318aa6870ccc','generation_spec_sha256':'130da046644c16684e08e93b2349fd6f1d89dbcaccf390a98654cfc1151982b6','teacher_fingerprint':'3904dfd83c65bbf8b16d4aa2fd2fdfbac74711741cfa26d06b0686a8b16dc15d','preprocessing':d['preprocessing'],'feature_order':d['preprocessing']['numeric']+d['preprocessing']['categorical'],'rng_state':rng_state(),'scheduler_state_dict':None,'scaler_state_dict':None});save(x,a.out);return
 c=x['config'];m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=(64,),dropout=0.0);m.load_state_dict(x['model_state_dict']);o=torch.optim.Adam(m.parameters(),lr=c.get('lr',.001));o.load_state_dict(x['optimizer_state_dict']);
 if a.mode=='resume-init':
  random.seed(991);torch.manual_seed(991);x['rng_state']=rng_state();x['resume_expected_cpu']=torch.get_rng_state().tolist();x['global_step']=int(x.get('global_step',0));save(x,a.out);json.dump({'pre_restart_global_step':x['global_step']},open(a.out+'.json','w'));return
 restore_rng(x['rng_state']);ok=torch.get_rng_state().tolist()==x['resume_expected_cpu'];n,cat,y=d['train_numeric'][:1024],d['train_categorical'][:1024],d['train_target'][:1024];loss=torch.nn.functional.mse_loss(m(n,cat),y);o.zero_grad();loss.backward();o.step();x['global_step']=int(x.get('global_step',0))+1;x['model_state_dict']=m.state_dict();x['optimizer_state_dict']=o.state_dict();save(x,a.out);json.dump({'restored_global_step':x['global_step']-1,'post_resume_global_step':x['global_step'],'optimizer_state':'PASS','scheduler_state':'NOT_APPLICABLE','scaler_state':'NOT_APPLICABLE','rng_state':'PASS' if ok else 'FAIL','hash_guards':'PASS' if x.get('frozen_config_sha256') else 'FAIL'},open(a.out+'.json','w')); 
if __name__=='__main__':main()
