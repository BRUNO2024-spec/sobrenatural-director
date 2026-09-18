#!/usr/bin/env python3
"""Deterministic SHA-256 fingerprint of a server mods directory."""
import argparse
import hashlib
from pathlib import Path

def digest(path):
    h=hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024*1024), b""):
            h.update(block)
    return h.hexdigest()

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("mods", type=Path)
    args=parser.parse_args()
    entries=[]
    for path in sorted(args.mods.glob("*")):
        if path.is_file() and path.suffix.lower() in (".jar", ".zip"):
            entries.append((path.name,digest(path)))
    canonical="".join(name+"="+sha+";" for name,sha in entries).encode("utf-8")
    print("SERVER_MOD_COUNT="+str(len(entries)))
    print("SERVER_MOD_STACK_FINGERPRINT="+hashlib.sha256(canonical).hexdigest())
    for name,sha in entries:
        print("MOD="+name+" SHA256="+sha)
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
