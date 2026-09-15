#!/usr/bin/env python3
import json
import pathlib
import sys

REQUIRED = {
    "BENCHMARK_SCENARIO_COVERAGE": "PASS",
    "BENCHMARK_SPLIT_LEAKAGE_AUDIT": "PASS",
    "BENCHMARK_DETERMINISM": "PASS",
    "BENCHMARK_SCORING_VALIDATION": "PASS",
    "BENCHMARK_SEQUENCE_VALIDATION": "PASS",
    "BENCHMARK_ZERO_MUTATION": "PASS",
    "BENCHMARK_BASELINE_RUN": "PASS",
}

def main():
    data = json.loads(pathlib.Path(sys.argv[1]).read_text())
    failures = [key + "=" + expected + " actual=" + str(data.get(key)) for key, expected in REQUIRED.items() if data.get(key) != expected]
    for key, minimum in (("SINGLE_STEP_SCENARIOS", 500), ("SEQUENCE_SCENARIOS", 50), ("TOTAL_DECISIONS", 1)):
        if data.get(key, 0) < minimum:
            failures.append(key + " below minimum")
    if data.get("HARD_SAFETY_VIOLATIONS") != 0:
        failures.append("HARD_SAFETY_VIOLATIONS must be 0")
    for key in ("BENCHMARK_WORLD_MUTATIONS", "BENCHMARK_ENTITY_SPAWNS", "BENCHMARK_FORCE_LOADS", "BENCHMARK_MPE_EXECUTIONS", "BENCHMARK_NATURAL_PRESSURE_ACTIONS"):
        if data.get(key) != 0:
            failures.append(key + " must be 0")
    if failures:
        for failure in failures:
            print("FAIL: " + failure)
        print("DIRECTOR_BENCHMARK_FOUNDATION_GATE=FAIL")
        return 1
    print("DIRECTOR_BENCHMARK_FOUNDATION_GATE=PASS")
    return 0

if __name__ == "__main__":
    sys.exit(main())
