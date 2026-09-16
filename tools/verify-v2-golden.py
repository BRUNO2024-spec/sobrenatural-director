"""Small fail-closed verifier for versioned golden-vector metadata."""
import hashlib,json,sys
from pathlib import Path
vectors=Path(sys.argv[1] if len(sys.argv)>1 else 'runtime-tests/benchmark/learned-scorer-v2-golden-vectors.jsonl')
manifest=vectors.with_suffix('.properties'); lines=vectors.read_bytes().splitlines(); meta={}
for line in manifest.read_text().splitlines():
    if '=' in line: k,v=line.split('=',1); meta[k]=v
if len(lines)<32 or hashlib.sha256(vectors.read_bytes()).hexdigest()!=meta.get('SHA256') or int(meta.get('COUNT','0'))!=len(lines): raise SystemExit('golden vector gate failed')
for line in lines:
    v=json.loads(line)
    if v.get('schema')!='DIRECTOR_EXPERIENCE_FEATURES_V3' or len(v.get('numeric_normalized',[]))!=19 or len(v.get('categorical_ids',[]))!=8: raise SystemExit('invalid vector')
print('golden vectors PASS: %d' % len(lines))
