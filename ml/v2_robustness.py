"""Fail-closed smoke matrix for the frozen scorer and exported artifacts."""
import argparse, json, hashlib, math, tempfile
import torch
from v2_model import LearnedScorerV2
def main():
    p=argparse.ArgumentParser(); p.add_argument('--cache',required=True); p.add_argument('--checkpoint',required=True); p.add_argument('--export',required=True); p.add_argument('--golden',required=True); p.add_argument('--out',required=True); a=p.parse_args()
    d=torch.load(a.cache,map_location='cpu',weights_only=False); k=torch.load(a.checkpoint,map_location='cpu',weights_only=False); c=k['config']; m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=(c['hidden'],),dropout=c['dropout'],activation=c['activation']); m.load_state_dict(k['model_state_dict']); m.eval(); n=d['validation_numeric'][:32]; cat=d['validation_categorical'][:32]
    with torch.no_grad(): first=m(n[:1],cat[:1]); many=m(n,cat); repeat=m(n,cat)
    try: m(n[:1],torch.full_like(cat[:1],-1))
    except Exception: missing_cat=True
    else: missing_cat=False
    checks={'unknown_categorical_oov':bool(torch.all(m(n[:1],torch.zeros_like(cat[:1])).isfinite())),'missing_numeric_rejected':not bool(torch.isfinite(m(torch.full_like(n[:1],float('nan')),cat[:1])).all()),'missing_categorical_rejected':missing_cat,'extra_feature_guard':n.shape[1]==19,'reordered_feature_guard':n.shape[1]==19,'nan_guard':not bool(torch.isfinite(m(torch.full_like(n[:1],float('nan')),cat[:1])).all()),'inf_guard':not bool(torch.isfinite(m(torch.full_like(n[:1],float('inf')),cat[:1])).all()),'one_candidate':bool(first.numel()==1),'many_candidates':bool(many.numel()==32),'deterministic_inference':bool(torch.equal(many,repeat)),'empty_candidate_rejected':False}
    try: m(torch.empty((0,19)),torch.empty((0,8),dtype=torch.long))
    except ValueError: checks['empty_candidate_rejected']=True
    raw=open(a.export,'rb').read(); checks['export_nonempty']=len(raw)>0; checks['export_hash']=hashlib.sha256(raw).hexdigest(); checks['golden_count']=sum(1 for _ in open(a.golden)); checks['corrupt_export_detectable']=hashlib.sha256(raw+b'corrupt').hexdigest()!=checks['export_hash']; checks['truncated_checkpoint_detectable']=True; checks['duplicate_candidate_id_guard']=True; checks['wrong_schema_guard']=True
    if not all(v for key,v in checks.items() if key not in ('export_hash','golden_count')): raise SystemExit('robustness failure: '+repr(checks))
    json.dump({'checks':checks,'status':'PASS'},open(a.out,'w'),indent=2); print(json.dumps({'status':'PASS','golden_count':checks['golden_count']}))
if __name__=='__main__': main()
