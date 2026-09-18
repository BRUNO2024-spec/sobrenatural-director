#!/usr/bin/env python3
"""Read-only provider status summary from a dedicated server console log."""
import argparse
import re
from pathlib import Path

ENTRY = re.compile(r"Provider registry entry id=([^,]+), status=([^,]+), capabilities=\[([^]]*)\]")
FINGERPRINT = re.compile(r"Provider registry fingerprint=([0-9a-f]{64})")

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--log", type=Path, default=Path("/home/desktop/Documentos/director-shadow-server/logs/console.log"))
    args = parser.parse_args()
    if not args.log.is_file():
        print("SERVER_RUNNING=UNKNOWN")
        print("PROVIDER_STATUS=LOG_NOT_FOUND")
        return 2
    text = args.log.read_text(encoding="utf-8", errors="replace")
    entries = list(ENTRY.finditer(text))
    if not entries:
        print("REGISTERED_PROVIDER_COUNT=0")
        print("PROVIDER_STATUS=NO_RUNTIME_REGISTRY_ENTRY")
        return 0
    latest = {}
    for match in entries:
        latest[match.group(1)] = (match.group(2), match.group(3))
    print("REGISTERED_PROVIDER_COUNT=" + str(len(latest)))
    fingerprints = FINGERPRINT.findall(text)
    if fingerprints:
        print("PROVIDER_REGISTRY_FINGERPRINT=" + fingerprints[-1])
    for provider_id in sorted(latest):
        status, capabilities = latest[provider_id]
        print("PROVIDER_ID=" + provider_id + " STATUS=" + status + " CAPABILITIES=" + capabilities)
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
