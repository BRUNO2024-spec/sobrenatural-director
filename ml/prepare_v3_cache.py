"""Materialize an ephemeral tensor cache from V3; cache stays outside Git."""
import argparse, hashlib, json
from pathlib import Path
import torch
from v3_dataset import iter_rows
from v3_preprocess import fit, encode
def main():
    p=argparse.ArgumentParser(); p.add_argument("root"); p.add_argument("--out",required=True); p.add_argument("--expected-manifest",default=""); args=p.parse_args(); root=Path(args.root); manifest=root/"manifest.json"; mh=hashlib.sha256(manifest.read_bytes()).hexdigest()
    if args.expected_manifest and mh!=args.expected_manifest: raise SystemExit("dataset manifest mismatch")
    prep=fit(root); result={"numeric_dim":len(prep["numeric"]),"vocab_sizes":[len(prep["vocab"][k]) for k in prep["categorical"]],"preprocessing":prep,"manifest_sha":mh}
    for split in ("TRAIN","VALIDATION","TEST","HOLDOUT"):
        xs=[]; cs=[]; ys=[]; gs=[]; group={}
        for r in iter_rows(root,split):
            x,c,y=encode(r,prep); xs.append(x); cs.append(c); ys.append(y); sid=r["metadata"]["stateId"]; group.setdefault(sid,len(group)); gs.append(group[sid])
        result[split.lower()+"_numeric"]=torch.tensor(xs,dtype=torch.float32); result[split.lower()+"_categorical"]=torch.tensor(cs,dtype=torch.long); result[split.lower()+"_target"]=torch.tensor(ys,dtype=torch.float32); result[split.lower()+"_groups"]=torch.tensor(gs,dtype=torch.long)
    out=Path(args.out); out.parent.mkdir(parents=True,exist_ok=True); torch.save(result,out); print(json.dumps({"manifest_sha":mh,"rows":{s:len(result[s.lower()+"_target"]) for s in ("TRAIN","VALIDATION","TEST","HOLDOUT")},"numeric_dim":result["numeric_dim"],"vocab_sizes":result["vocab_sizes"]}))
if __name__=="__main__": main()
