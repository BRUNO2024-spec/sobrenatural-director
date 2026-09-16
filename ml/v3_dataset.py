"""Streaming V3 reader with explicit locked-split training guard."""
import hashlib, json, math
from pathlib import Path
SCHEMA="DIRECTOR_EXPERIENCE_FEATURES_V3"
def iter_rows(root, split):
    if split not in ("TRAIN","VALIDATION","TEST","HOLDOUT"): raise ValueError("invalid split")
    for path in sorted((Path(root)/split).glob("part-*.jsonl")):
        for line in path.read_text(encoding="utf-8").splitlines():
            r=json.loads(line)
            if r["metadata"]["schemaVersion"]!=SCHEMA or r["metadata"]["split"]!=split: raise ValueError("schema/split mismatch")
            yield r
def training_rows(root, split="TRAIN"):
    if split!="TRAIN": raise ValueError("TEST/HOLDOUT/VALIDATION cannot be training source")
    return iter_rows(root,split)
def input_object(row):
    return {"stateFeatures":row["stateFeatures"],"candidateFeatures":row["candidateFeatures"]}
def fingerprint(row): return hashlib.sha256(json.dumps(input_object(row),sort_keys=True,separators=(",",":"),ensure_ascii=True).encode()).hexdigest()
def validate_row(row):
    m=row.get("metadata",{}); sup=row.get("supervision",{}); inp=input_object(row)
    if m.get("schemaVersion")!=SCHEMA or not m.get("stateId") or not m.get("candidateId"): raise ValueError("invalid V3 metadata")
    if m.get("split") not in ("TRAIN","VALIDATION","TEST","HOLDOUT") or m.get("trainingAllowed")!=(m["split"]=="TRAIN"): raise ValueError("invalid split policy")
    forbidden=("teacherQuality","teacherChosen","policyDecision","selected","stateId","candidateId","seed","split","trainingAllowed")
    if any(k in inp[part] for part in ("stateFeatures","candidateFeatures") for k in forbidden): raise ValueError("leakage")
    if not math.isfinite(float(sup["teacherQuality"])): raise ValueError("invalid target")
