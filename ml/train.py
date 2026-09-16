import argparse, json, random, hashlib
from pathlib import Path
import torch
from model import LearnedScorerV1
from checkpointing import atomic_save, load_compatible, rng_state, restore_rng

def load(path, split):
    return [json.loads(x) for x in Path(path).read_text().splitlines() if json.loads(x)["split"]==split]
def main():
    p=argparse.ArgumentParser(); p.add_argument("--dataset",required=True); p.add_argument("--checkpoint-dir",required=True); p.add_argument("--epochs",type=int,default=20); p.add_argument("--max-steps",type=int,default=0); p.add_argument("--seed",type=int,default=7); p.add_argument("--resume",default="none"); p.add_argument("--hidden",type=int,default=32); p.add_argument("--dropout",type=float,default=0.0); p.add_argument("--learning-rate",type=float,default=1e-3); p.add_argument("--weight-decay",type=float,default=0.0); p.add_argument("--output-metrics",default=""); p.add_argument("--run-id",default="learned-scorer-v1"); args=p.parse_args()
    random.seed(args.seed); torch.manual_seed(args.seed); d=Path(args.dataset); manifest=json.loads((d/"manifest.json").read_text()); rows=load(d/"rows.jsonl","TRAIN"); val=load(d/"rows.jsonl","VALIDATION")
    prep=manifest["preprocessing"]; vocab_sizes=[len(prep["vocab"][k]) for k in prep["categorical"]]
    device=torch.device("cuda" if torch.cuda.is_available() else "cpu"); model=LearnedScorerV1(len(prep["numeric"]),vocab_sizes,hidden=args.hidden,dropout=args.dropout).to(device); opt=torch.optim.Adam(model.parameters(),lr=args.learning_rate,weight_decay=args.weight_decay); lossfn=torch.nn.MSELoss(); step=0; best=float("inf")
    schema=manifest["schemaVersion"]; mh=hashlib.sha256((d/"manifest.json").read_bytes()).hexdigest(); order=manifest["featureOrder"]; ck=Path(args.checkpoint_dir); ck.mkdir(parents=True,exist_ok=True)
    if args.resume not in ("none",):
        path=ck/"latest.pt" if args.resume=="auto" else Path(args.resume); c=load_compatible(path,schema,mh,order); model.load_state_dict(c["model_state_dict"]); opt.load_state_dict(c["optimizer_state_dict"]); step=c["global_step"]; best=c["best_metric"]; restore_rng(c["rng_state"])
    def batch(rs): return torch.tensor([r["numeric"] for r in rs],dtype=torch.float32,device=device),torch.tensor([r["categorical"] for r in rs],dtype=torch.long,device=device),torch.tensor([r["target"] for r in rs],dtype=torch.float32,device=device)
    for epoch in range(args.epochs):
        model.train(); random.shuffle(rows); x,c,y=batch(rows); pred=model(x,c); loss=lossfn(pred,y); opt.zero_grad(); loss.backward(); opt.step(); step+=1
        model.eval(); vx,vc,vy=batch(val); vloss=float(lossfn(model(vx,vc),vy).item());
        payload={"model_state_dict":model.state_dict(),"optimizer_state_dict":opt.state_dict(),"scheduler_state_dict":None,"scaler_state_dict":None,"epoch":epoch+1,"global_step":step,"best_metric":min(best,vloss),"config":vars(args),"feature_schema":schema,"feature_order":order,"dataset_manifest_hash":mh,"source_commit":manifest.get("sourceCommit"),"rng_state":rng_state(),"training_run_id":args.run_id}
        atomic_save(payload,ck/"latest.pt");
        if vloss<=best: best=vloss; payload["best_metric"]=best; atomic_save(payload,ck/"best.pt")
        event={"epoch":epoch+1,"step":step,"train_mse":float(loss.item()),"validation_mse":vloss,"cuda":torch.cuda.is_available(),"gpu_count":torch.cuda.device_count()}; print(json.dumps(event),flush=True)
        if args.output_metrics: open(args.output_metrics,"a").write(json.dumps(event)+"\n")
        if args.max_steps and step>=args.max_steps: break
if __name__=="__main__": main()
