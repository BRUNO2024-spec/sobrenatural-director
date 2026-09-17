#!/usr/bin/env python3
"""Small, dependency-free V1/V2 reader and canonicalizer.

It never mutates source JSONL and refuses to canonicalize legacy V1 or records
without persisted consent/session evidence.
"""
import json
from pathlib import Path

V2 = "DIRECTOR_SHADOW_EXPERIENCE_V2"


def read_jsonl(path):
    rows = []
    errors = []
    for number, line in enumerate(Path(path).read_text(encoding="utf-8").splitlines(), 1):
        try:
            rows.append(json.loads(line))
        except Exception as exc:
            errors.append((number, str(exc)))
    return rows, errors


def validate_v2(path):
    rows, errors = read_jsonl(path)
    sequences = [r.get("eventSequence") for r in rows]
    result = {"parseErrors": len(errors), "duplicates": 0, "orphans": 0,
              "trainingAllowedTrue": sum(r.get("trainingAllowed") is True for r in rows),
              "events": len(rows), "decisions": 0, "canonicalizable": 0}
    seen = set()
    for sequence in sequences:
        if sequence in seen:
            result["duplicates"] += 1
        seen.add(sequence)
    grouped = {}
    consented = set()
    for row in rows:
        if row.get("schemaVersion") != V2:
            continue
        if row.get("eventType") == "PARTICIPANT_SESSION_START":
            consented.add(row.get("participantSessionId"))
        decision = row.get("decisionId")
        if decision:
            grouped.setdefault(decision, set()).add(row.get("eventType"))
    for event_types in grouped.values():
        if "DECISION_SNAPSHOT" in event_types:
            result["decisions"] += 1
            no_action = "DECISION_TERMINAL" in event_types
            action_terminal = any(x in event_types for x in ("EXECUTION_COMPLETED", "EXECUTION_ABORTED", "EXECUTION_REPLANNED", "EXECUTION_SAFETY_REJECTED", "EXECUTION_TIMED_OUT"))
            if no_action or action_terminal:
                result["canonicalizable"] += 1
    return result


def canonicalize_v2(source, destination):
    source = Path(source)
    rows, errors = read_jsonl(source)
    if errors or not rows or any(row.get("schemaVersion") != V2 for row in rows):
        raise ValueError("only complete V2 JSONL may be canonicalized")
    starts = {r.get("participantSessionId") for r in rows if r.get("eventType") == "PARTICIPANT_SESSION_START"}
    if not starts:
        raise ValueError("consent provenance is required")
    by_decision = {}
    for row in rows:
        if row.get("decisionId"):
            by_decision.setdefault(row["decisionId"], []).append(row)
    output = []
    for decision_id, events in by_decision.items():
        kinds = {e.get("eventType") for e in events}
        if not {"DECISION_SNAPSHOT", "ACTUAL_DECISION", "SHADOW_SCORE_RESULT"}.issubset(kinds):
            continue
        terminal = "NO_ACTION" if "DECISION_TERMINAL" in kinds else next((k for k in kinds if k.startswith("EXECUTION_")), None)
        if terminal is None:
            continue
        first = events[0]
        output.append({"schemaVersion": "REAL_DIRECTOR_EXPERIENCE_V1", "sessionId": first["collectionSessionId"],
                       "decisionId": decision_id, "actual": next(e.get("actualCandidateId") for e in events if e.get("eventType") == "ACTUAL_DECISION"),
                       "shadow": next(e.get("shadowSelectedId") for e in events if e.get("eventType") == "SHADOW_SCORE_RESULT"),
                       "outcome": terminal, "trainingAllowed": False,
                       "counterfactualOutcomeAvailable": False})
    destination = Path(destination)
    destination.parent.mkdir(parents=True, exist_ok=True)
    with destination.open("w", encoding="utf-8") as stream:
        for row in output:
            stream.write(json.dumps(row, sort_keys=True) + "\n")
    return len(output)
