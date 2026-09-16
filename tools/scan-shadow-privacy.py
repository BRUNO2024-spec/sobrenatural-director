#!/usr/bin/env python3
"""Fail-closed scan for common raw identity/chat fields in local research logs."""
import json, pathlib, re, sys
KEYS = re.compile(r'(^|_)(username|playername|rawuuid|uuid|ip|ipaddress|chat|message)(_|$)', re.I)
UUID = re.compile(r'\b[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\b', re.I)
IP = re.compile(r'\b(?:\d{1,3}\.){3}\d{1,3}\b')
def scan(root):
    hits=[]
    for p in pathlib.Path(root).glob('**/*.jsonl'):
        for n, line in enumerate(p.read_text(encoding='utf-8').splitlines(),1):
            try: obj=json.loads(line)
            except json.JSONDecodeError: hits.append((str(p),n,'invalid-json')); continue
            if any(KEYS.search(str(k)) for k in obj): hits.append((str(p),n,'identity-key'))
            if UUID.search(line): hits.append((str(p),n,'uuid'))
            if IP.search(line): hits.append((str(p),n,'ip'))
    return hits
if __name__ == '__main__':
    hits=scan(sys.argv[1] if len(sys.argv)>1 else '.')
    print(json.dumps({'privacyPass':not hits,'hits':hits}, sort_keys=True))
    raise SystemExit(1 if hits else 0)
