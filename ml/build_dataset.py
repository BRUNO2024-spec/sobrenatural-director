"""Build a small JSONL dataset and manifest; no materialized dataset is committed."""
import argparse, hashlib, json, subprocess, time
from pathlib import Path
from data import read_rows, fit_transform, encode, SCHEMA

def main():
    ap=argparse.ArgumentParser(); ap.add_argument("--source",required=True); ap.add_argument("--out",required=True); ap.add_argument("--seed",type=int,default=20260916); args=ap.parse_args()
    out=Path(args.out); out.mkdir(parents=True,exist_ok=True); source=Path(args.source)
    allrows=read_rows(source); train=[r for r in allrows if r["split"]=="TRAIN"]; val=[r for r in allrows if r["split"]=="VALIDATION"]
    if not train: raise ValueError("no TRAIN rows")
    prep=fit_transform(train)
    counts={s:sum(r["split"]==s for r in allrows) for s in ("TRAIN","VALIDATION","TEST","HOLDOUT")}
    manifest={"schemaVersion":SCHEMA,"sourceSha256":hashlib.sha256(source.read_bytes()).hexdigest(),"featureOrder":prep["numeric"]+prep["categorical"],"preprocessing":prep,"splitCounts":counts,"seed":args.seed,"runId":"dataset-"+hashlib.sha256(source.read_bytes()).hexdigest()[:12],"timestampUtc":time.strftime("%Y-%m-%dT%H:%M:%SZ",time.gmtime())}
    try: manifest["sourceCommit"]=subprocess.check_output(["git","rev-parse","HEAD"],text=True).strip()
    except Exception: manifest["sourceCommit"]="unknown"
    (out/"manifest.json").write_text(json.dumps(manifest,sort_keys=True,indent=2)+"\n",encoding="utf-8")
    with (out/"rows.jsonl").open("w",encoding="utf-8") as f:
        for r in allrows:
            x,c,y=encode(r,prep); f.write(json.dumps({"scenarioId":r["scenarioId"],"split":r["split"],"numeric":x,"categorical":c,"target":y},sort_keys=True)+"\n")
    print(json.dumps({"rows":len(allrows),"train":len(train),"validation":len(val),"dimensions":len(prep["numeric"]),"categoricalVocab":{k:len(v) for k,v in prep["vocab"].items()}}))
if __name__=="__main__": main()
