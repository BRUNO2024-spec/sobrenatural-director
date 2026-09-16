"""Materialize an ephemeral tensor cache for V4 development splits only."""
import argparse, hashlib, json
from pathlib import Path
import torch
from v4_dataset import iter_rows
from v4_preprocess import fit, encode

def main():
    p=argparse.ArgumentParser(); p.add_argument('root'); p.add_argument('--out',required=True); p.add_argument('--expected-manifest',required=True); a=p.parse_args()
    root=Path(a.root); mh=hashlib.sha256((root/'manifest.json').read_bytes()).hexdigest()
    if mh != a.expected_manifest: raise SystemExit('dataset manifest mismatch')
    prep=fit(root); out={'numeric_dim':len(prep['numeric']),'vocab_sizes':[len(prep['vocab'][k]) for k in prep['categorical']],'preprocessing':prep,'manifest_sha':mh}
    for name in ('TRAIN','VALIDATION_IID','VALIDATION_STRESS'):
        xs=[];cs=[];ys=[];gs=[];group={}
        for r in iter_rows(root,name):
            x,c,y=encode(r,prep); xs.append(x);cs.append(c);ys.append(y); sid=r['metadata']['stateId'];group.setdefault(sid,len(group));gs.append(group[sid])
        key=name.lower();out[key+'_numeric']=torch.tensor(xs,dtype=torch.float32);out[key+'_categorical']=torch.tensor(cs,dtype=torch.long);out[key+'_target']=torch.tensor(ys,dtype=torch.float32);out[key+'_groups']=torch.tensor(gs,dtype=torch.long)
    target=Path(a.out);target.parent.mkdir(parents=True,exist_ok=True);torch.save(out,target);print(json.dumps({'manifest_sha':mh,'rows':{k:len(out[k+'_target']) for k in ('train','validation_iid','validation_stress')},'numeric_dim':out['numeric_dim']}))
if __name__=='__main__':main()
