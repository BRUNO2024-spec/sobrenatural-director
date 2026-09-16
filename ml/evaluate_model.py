import argparse, json, math, statistics, hashlib
from pathlib import Path
import torch
from data import read_rows, encode
from model import LearnedScorerV1
from checkpointing import load_compatible
from metrics import regression, group_metrics, mean

def main():
    p=argparse.ArgumentParser(); p.add_argument("--dataset",required=True); p.add_argument("--checkpoint",required=True); p.add_argument("--split",choices=["TRAIN","VALIDATION","TEST","HOLDOUT"],required=True); p.add_argument("--manifest",default=""); p.add_argument("--out",required=True); args=p.parse_args()
    d=Path(args.dataset); manifest=json.loads((d/"manifest.json").read_text()); prep=manifest["preprocessing"]; rows=read_rows(Path(args.dataset).parent.parent/"runtime-tests/benchmark/director-benchmark-v2-features-A.jsonl",args.split) if False else None
    # Materialized rows preserve only encoded input and target, so no locked split is opened implicitly.
    raw=[json.loads(x) for x in (d/"rows.jsonl").read_text().splitlines() if json.loads(x)["split"]==args.split]
    mh=hashlib.sha256((d/"manifest.json").read_bytes()).hexdigest(); order=manifest["featureOrder"]
    vocab_sizes=[len(prep["vocab"][k]) for k in prep["categorical"]]; c=load_compatible(args.checkpoint,manifest["schemaVersion"],mh,order)
    model=LearnedScorerV1(len(prep["numeric"]),vocab_sizes,hidden=c["config"].get("hidden",32),dropout=c["config"].get("dropout",0.0)).eval(); model.load_state_dict(c["model_state_dict"])
    with torch.no_grad(): pred=model(torch.tensor([r["numeric"] for r in raw],dtype=torch.float32),torch.tensor([r["categorical"] for r in raw],dtype=torch.long)).tolist()
    scored=[{"scenarioId":r["scenarioId"],"candidateId":r.get("candidateId",""),"prediction":x,"target":r["target"]} for r,x in zip(raw,pred)]; groups={}
    for r in scored: groups.setdefault(r["scenarioId"],[]).append(r)
    target=[r["target"] for r in scored]; result={"split":args.split,"rows":len(raw),"regression":regression(pred,target),"ranking":group_metrics(groups),"target_mean":mean(target),"target_std":statistics.pstdev(target) if len(target)>1 else 0.0}
    Path(args.out).write_text(json.dumps(result,sort_keys=True,indent=2)+"\n"); print(json.dumps(result,sort_keys=True))
if __name__=="__main__": main()
