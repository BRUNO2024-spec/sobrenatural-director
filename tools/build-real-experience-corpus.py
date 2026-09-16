#!/usr/bin/env python3
"""Validate and stage local shadow JSONL without assigning training labels."""
import argparse, json, pathlib, hashlib, sys

SCHEMA = "DIRECTOR_SHADOW_EXPERIENCE_V1"

def build(source, output):
    source, output = pathlib.Path(source), pathlib.Path(output)
    records, errors, decisions = [], [], set()
    for path in sorted(source.glob("**/*.jsonl")):
        for line_no, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            try:
                item = json.loads(raw)
                if item.get("schema") != SCHEMA: raise ValueError("wrong schema")
                if item.get("eventType") == "DECISION_SNAPSHOT":
                    did = item.get("decisionId")
                    if not did or did in decisions: raise ValueError("duplicate decision")
                    decisions.add(did)
                item["trainingAllowed"] = False
                records.append(item)
            except (ValueError, json.JSONDecodeError) as exc:
                errors.append({"file": str(path), "line": line_no, "error": str(exc)})
    if errors: raise ValueError(json.dumps({"errors": errors}, sort_keys=True))
    output.mkdir(parents=True, exist_ok=True)
    out = output / "canonical.jsonl"
    with out.open("w", encoding="utf-8") as handle:
        for item in records: handle.write(json.dumps(item, sort_keys=True, separators=(",", ":")) + "\n")
    manifest = {"schema": SCHEMA, "trainingAllowed": False, "records": len(records),
                "sha256": hashlib.sha256(out.read_bytes()).hexdigest()}
    (output / "audit.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return manifest

if __name__ == "__main__":
    parser = argparse.ArgumentParser(); parser.add_argument("source"); parser.add_argument("output")
    args = parser.parse_args()
    try: print(json.dumps(build(args.source, args.output), sort_keys=True))
    except ValueError as exc: print(str(exc), file=sys.stderr); raise SystemExit(2)
