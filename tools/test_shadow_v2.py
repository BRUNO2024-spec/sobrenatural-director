#!/usr/bin/env python3
import json
import tempfile
from pathlib import Path
from shadow_v2_pipeline import canonicalize_v2, validate_v2, V2


def main():
    with tempfile.TemporaryDirectory(prefix="shadow-v2-python-") as directory:
        source = Path(directory) / "events.jsonl"
        rows = [{"schemaVersion": V2, "eventType": "PARTICIPANT_SESSION_START", "collectionSessionId": "cs_TEST", "participantSessionId": "ps_TEST", "trainingAllowed": False, "eventSequence": 1}]
        for i in range(1000):
            base = 2 + i * 4
            decision = "d%d" % i
            rows += [
                {"schemaVersion": V2, "eventType": "DECISION_SNAPSHOT", "collectionSessionId": "cs_TEST", "decisionId": decision, "eventSequence": base, "trainingAllowed": False},
                {"schemaVersion": V2, "eventType": "ACTUAL_DECISION", "collectionSessionId": "cs_TEST", "decisionId": decision, "actualCandidateId": "candidate:no_action", "eventSequence": base + 1, "trainingAllowed": False},
                {"schemaVersion": V2, "eventType": "SHADOW_SCORE_RESULT", "collectionSessionId": "cs_TEST", "decisionId": decision, "shadowSelectedId": "candidate:no_action", "eventSequence": base + 2, "trainingAllowed": False},
                {"schemaVersion": V2, "eventType": "DECISION_TERMINAL", "collectionSessionId": "cs_TEST", "decisionId": decision, "terminalType": "NO_ACTION", "eventSequence": base + 3, "trainingAllowed": False},
            ]
        source.write_text("\n".join(json.dumps(row) for row in rows) + "\n", encoding="utf-8")
        result = validate_v2(source)
        assert result["parseErrors"] == result["duplicates"] == result["orphans"] == result["trainingAllowedTrue"] == 0
        assert result["decisions"] == result["canonicalizable"] == 1000
        destination = Path(directory) / "canonical.jsonl"
        assert canonicalize_v2(source, destination) == 1000
    print("PYTHON_TESTS=PASS")
    print("PYTHON_TEST_COUNT=1")


if __name__ == "__main__":
    main()
