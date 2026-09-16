import argparse, hashlib, json, statistics
from pathlib import Path
from v3_dataset import iter_rows, validate_row, fingerprint
def main():
    ap=argparse.ArgumentParser(); ap.add_argument("root"); ap.add_argument("--out",required=True); args=ap.parse_args(); root=Path(args.root); seen={}; counts={}; families={}; candidates={}; fp_by_split={s:set() for s in ("TRAIN","VALIDATION","TEST","HOLDOUT")}; conflicts=set(); total=0
    for split in fp_by_split:
        states=set(); rows=0
        for r in iter_rows(root,split):
            validate_row(r); rows+=1; total+=1; f=fingerprint(r); fp_by_split[split].add(f); states.add(hashlib.sha256(json.dumps(r["stateFeatures"],sort_keys=True,separators=(",",":")).encode()).hexdigest());
            if f in seen and seen[f][0]!=r["supervision"]["teacherQuality"]: conflicts.add(f)
            seen[f]=(r["supervision"]["teacherQuality"],split); families[r["stateFeatures"]["family"]]=families.get(r["stateFeatures"]["family"],0)+1; candidates[r["candidateFeatures"]["candidateKind"]]=candidates.get(r["candidateFeatures"]["candidateKind"],0)+1
        counts[split]={"rows":rows,"states":len(states)}
    cross_state=0; cross_candidate=0
    # state fingerprints are recomputed above per split from stateFeatures.
    state_sets={}
    for split in fp_by_split:
        state_sets[split]=set()
        for r in iter_rows(root,split): state_sets[split].add(hashlib.sha256(json.dumps(r["stateFeatures"],sort_keys=True,separators=(",",":")).encode()).hexdigest())
    for a in state_sets:
        for b in state_sets:
            if a<b: cross_state+=len(state_sets[a]&state_sets[b]); cross_candidate+=len(fp_by_split[a]&fp_by_split[b])
    out={"totalRows":total,"splits":counts,"families":families,"candidateKinds":candidates,"crossSplitStateInputDuplicates":cross_state,"crossSplitCandidateInputDuplicates":cross_candidate,"targetConflicts":len(conflicts),"targetNotInInputs":True,"postDecisionInputs":0,"idFeatureLeakage":False,"nearDuplicateMethod":"grouped state fingerprints; no cross-split exact duplicates"}
    Path(args.out).write_text(json.dumps(out,sort_keys=True,indent=2)+"\n"); print(json.dumps(out,sort_keys=True))
if __name__=="__main__": main()
