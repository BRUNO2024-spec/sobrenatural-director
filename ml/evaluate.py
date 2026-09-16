"""Evaluation helper intentionally permits only VALIDATION by default."""
import argparse, json
from pathlib import Path
def main():
    p=argparse.ArgumentParser(); p.add_argument("--dataset",required=True); p.add_argument("--split",choices=["VALIDATION","TEST","HOLDOUT"],default="VALIDATION"); args=p.parse_args()
    rows=[json.loads(x) for x in Path(args.dataset,"rows.jsonl").read_text().splitlines() if json.loads(x)["split"]==args.split]
    print(json.dumps({"split":args.split,"rows":len(rows),"testOrHoldoutLocked":args.split in ("TEST","HOLDOUT")}))
if __name__=="__main__": main()
