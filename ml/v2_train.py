"""V2 research trainer. It consumes a preprocessed V3 tensor cache."""
import argparse, json, random, time
from pathlib import Path
import torch
from v2_model import LearnedScorerV2

def metrics(model,n,c,y,groups,device):
    model.eval(); out=[]
    with torch.no_grad():
        for i in range(0,len(y),8192): out += model(n[i:i+8192].to(device),c[i:i+8192].to(device)).cpu().tolist()
    target=y.tolist(); e=[a-b for a,b in zip(out,target)]; mse=sum(x*x for x in e)/len(e); groups_out={}
    for i,g in enumerate(groups.tolist()): groups_out.setdefault(g,[]).append((out[i],target[i]))
    regrets=[]; pairs=correct=0
    for vals in groups_out.values():
        pred=max(range(len(vals)),key=lambda i:vals[i][0]); best=max(v[1] for v in vals); regrets.append(best-vals[pred][1])
        for a in vals:
            for b in vals:
                if a[1]==b[1]: continue
                pairs+=1; correct += ((a[0]>b[0])==(a[1]>b[1]))
    mae=sum(abs(x) for x in e)/len(e)
    return {"mse":mse,"rmse":mse**.5,"mae":mae,"pairwise":correct/pairs if pairs else 0.0,"meanRegret":sum(regrets)/len(regrets),"groups":len(groups_out)}
def main():
    p=argparse.ArgumentParser(); p.add_argument("--cache",required=True); p.add_argument("--out",required=True); p.add_argument("--objective",choices=["pointwise","pairwise","hybrid"],default="pointwise"); p.add_argument("--hidden",type=int,default=64); p.add_argument("--layers",default="32"); p.add_argument("--dropout",type=float,default=0.0); p.add_argument("--activation",default="relu"); p.add_argument("--lr",type=float,default=1e-3); p.add_argument("--wd",type=float,default=0.0); p.add_argument("--seed",type=int,default=7); p.add_argument("--epochs",type=int,default=15); p.add_argument("--run-id",required=True); p.add_argument("--resume",default=""); args=p.parse_args()
    random.seed(args.seed); torch.manual_seed(args.seed); d=torch.load(args.cache,map_location="cpu",weights_only=False); device=torch.device("cuda" if torch.cuda.is_available() else "cpu"); hidden=tuple([args.hidden]+[int(x) for x in args.layers.split(",") if x]) if args.layers else (args.hidden,); model=LearnedScorerV2(d["numeric_dim"],d["vocab_sizes"],hidden=hidden,dropout=args.dropout,activation=args.activation).to(device); opt=torch.optim.Adam(model.parameters(),lr=args.lr,weight_decay=args.wd); best=1e99; best_rank=1e99; step=0; start=time.time(); out=Path(args.out); out.mkdir(parents=True,exist_ok=True)
    if args.resume:
        ck=torch.load(args.resume,map_location=device,weights_only=False); model.load_state_dict(ck["model_state_dict"]); opt.load_state_dict(ck["optimizer_state_dict"]); step=ck["global_step"]
    n,c,y,g=d["train_numeric"],d["train_categorical"],d["train_target"],d["train_groups"]; vn,vc,vy,vg=d["val_numeric"],d["val_categorical"],d["val_target"],d["val_groups"]; order=torch.randperm(len(y),generator=torch.Generator().manual_seed(args.seed));
    history=[]
    for epoch in range(args.epochs):
        model.train(); total=0.0
        for ix in order.split(2048):
            pred=model(n[ix].to(device),c[ix].to(device)); loss=torch.nn.functional.mse_loss(pred,y[ix].to(device))
            if args.objective in ("pairwise","hybrid"):
                # Same-state pairs are represented by target ordering only.
                penalty=torch.tensor(0.0,device=device); by={}
                for j in ix.tolist(): by.setdefault(int(g[j]),[]).append(j)
                for vals in by.values():
                    if len(vals)>1:
                        pv=pred[[list(ix).index(j) for j in vals]]; tv=y[vals].to(device); diff=tv[:,None]-tv[None,:]; penalty += torch.relu(0.02-(pv[:,None]-pv[None,:])*diff.sign())[diff.abs()>1e-9].mean()
                loss=penalty if args.objective=="pairwise" else loss+0.25*penalty
            opt.zero_grad(); loss.backward(); opt.step(); total+=float(loss.item()); step+=1
        val=metrics(model,vn,vc,vy,vg,device); event={"epoch":epoch+1,"global_step":step,"train_loss":total,"validation":val}; history.append(event); print(json.dumps(event),flush=True)
        if val["meanRegret"]<best_rank: best_rank=val["meanRegret"]; best=val["mse"]; payload={"model_state_dict":model.state_dict(),"optimizer_state_dict":opt.state_dict(),"global_step":step,"epoch":epoch+1,"best_metric":best_rank,"config":vars(args),"cache_sha":d["cache_sha"]}; torch.save(payload,out/"best.pt")
    torch.save(payload,out/"latest.pt"); json.dump({"runId":args.run_id,"config":vars(args),"history":history,"best":metrics(model,vn,vc,vy,vg,device),"elapsed":time.time()-start,"parameters":sum(x.numel() for x in model.parameters()),"device":str(device)},open(out/"result.json","w"),indent=2)
if __name__=="__main__": main()
