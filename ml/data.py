"""Strict, dependency-light reader and TRAIN-only preprocessing for V2."""
import json, math
from pathlib import Path

SCHEMA = "DIRECTOR_BENCHMARK_FEATURES_V2"
NUMERIC = ["hardValid", "noAction", "providerAvailable", "continuation", "recentHigh", "dimension", "baseUtility"]
CATEGORICAL = ["environment", "candidateKind", "intent", "safety", "provider"]

def read_rows(path, split=None):
    rows = []
    for line in Path(path).read_text(encoding="utf-8").splitlines():
        if not line.strip(): continue
        row = json.loads(line)
        if row.get("schemaVersion") != SCHEMA: raise ValueError("incompatible feature schema")
        validate_row(row)
        if split is None or row["split"] == split: rows.append(row)
    return rows

def validate_row(row):
    if not isinstance(row.get("stateFeatures"), dict) or not isinstance(row.get("candidateFeatures"), dict): raise ValueError("missing feature object")
    forbidden={"quality","policyDecision","decision","selectedBlueprint","selectedIntent","selectedProvider","selectedIntensity","latency","decisionLatencyNanos"}
    if forbidden.intersection(row["stateFeatures"]) or forbidden.intersection(row["candidateFeatures"]): raise ValueError("post-decision input")
    if row.get("split") not in ("TRAIN","VALIDATION","TEST","HOLDOUT"): raise ValueError("invalid split")
    if row["trainingAllowed"] != (row["split"]=="TRAIN"): raise ValueError("trainingAllowed mismatch")
    if not math.isfinite(float(row["target"]["quality"])): raise ValueError("non-finite target")
    for key in NUMERIC:
        if not math.isfinite(numeric(row,key)): raise ValueError("non-finite input")

def fit_transform(rows):
    if not rows or any(r["split"] != "TRAIN" for r in rows): raise ValueError("fit requires TRAIN only")
    means, stds = {}, {}
    for key in NUMERIC:
        vals = [numeric(r, key) for r in rows]; mean = sum(vals) / len(vals)
        var = sum((x-mean)**2 for x in vals) / len(vals)
        means[key], stds[key] = mean, max(var ** .5, 1e-12)
    vocab = {key: ["<UNK>"] + sorted({categorical(r, key) for r in rows}) for key in CATEGORICAL}
    return {"numeric": NUMERIC, "categorical": CATEGORICAL, "means": means, "stds": stds, "vocab": vocab}

def numeric(row, key):
    src = row["candidateFeatures"] if key == "baseUtility" else row["stateFeatures"]
    try: return float(src[key])
    except (KeyError, TypeError, ValueError) as e: raise ValueError("missing or invalid numeric feature") from e

def categorical(row, key):
    src = row["candidateFeatures"] if key in ("candidateKind", "intent", "safety", "provider") else row["stateFeatures"]
    try: return str(src[key])
    except KeyError as e: raise ValueError("missing categorical feature") from e

def encode(row, prep):
    x = [(numeric(row,k)-prep["means"][k])/prep["stds"][k] for k in prep["numeric"]]
    cats = [prep["vocab"][k].index(categorical(row,k)) if categorical(row,k) in prep["vocab"][k] else 0 for k in prep["categorical"]]
    return x, cats, float(row["target"]["quality"])
