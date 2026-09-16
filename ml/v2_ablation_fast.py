"""Diagnostic feature masks; never updates the frozen model."""
import argparse,json,torch
from v2_model import LearnedScorerV2
from v2_closure_fast import agg
def main():
 p=argparse.ArgumentParser();p.add_argument('--cache',required=True);p.add_argument('--checkpoint',required=True);p.add_argument('--out',required=True);a=p.parse_args();d=torch.load(a.cache,map_location='cpu',weights_only=False);k=torch.load(a.checkpoint,map_location='cpu',weights_only=False);c=k['config'];m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=(c['hidden'],),dropout=c['dropout'],activation=c['activation']);m.load_state_dict(k['model_state_dict']);n=d['validation_numeric'];cat=d['validation_categorical'];y=d['validation_target'].numpy();g=d['validation_groups'].numpy();base=m(n,cat).detach().numpy();res={'baseline':agg(base,y,g)}
 masks={'numeric_only':(n,torch.zeros_like(cat)),'categorical_only':(torch.zeros_like(n),cat),'no_memory_repetition':(n.clone(),cat),'no_provider_context':(n.clone(),cat),'no_temporal_context':(n.clone(),cat)};masks['no_memory_repetition'][0][:,[11,16]]=0;masks['no_provider_context'][0][:,[10]]=0;masks['no_provider_context'][1][:,[2,7]]=0;masks['no_temporal_context'][0][:,[13,14,15,17]]=0
 for name,(x,z) in masks.items(): res[name]=agg(m(x,z).detach().numpy(),y,g);res[name]['delta_pairwise']=res[name]['pairwise']-res['baseline']['pairwise'];res[name]['delta_regret']=res[name]['mean_regret']-res['baseline']['mean_regret']
 json.dump(res,open(a.out,'w'),indent=2);print(json.dumps(res))
if __name__=='__main__':main()
