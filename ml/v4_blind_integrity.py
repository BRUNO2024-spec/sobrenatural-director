"""Fail-closed isolation checks for the generated V4 blind corpus."""
import argparse,hashlib,json
from pathlib import Path
def rows(root,splits):
    for split in splits:
        for p in sorted((Path(root)/split).glob('part-*.jsonl')):
            for line in p.read_text(encoding='utf8').splitlines(): yield json.loads(line)
def fp(r): return hashlib.sha256(json.dumps({'stateFeatures':r['stateFeatures'],'candidateFeatures':r['candidateFeatures']},sort_keys=True,separators=(',',':')).encode()).hexdigest()
def main():
    p=argparse.ArgumentParser();p.add_argument('--blind',required=True);p.add_argument('--historical',nargs='+',required=True);p.add_argument('--development',required=True);p.add_argument('--out',required=True);a=p.parse_args()
    bf={fp(r) for r in rows(a.blind,('TEST','HOLDOUT'))};bc=set();be=set()
    for r in rows(a.blind,('TEST','HOLDOUT')): bc.add(r['metadata'].get('candidateId'));be.add(r['metadata'].get('episodeId'))
    hist=set();hc=set();he=set()
    for root in a.historical:
        for r in rows(root,('TRAIN','VALIDATION','TEST','HOLDOUT')): hist.add(fp(r));hc.add(r['metadata'].get('candidateId'));he.add(r['metadata'].get('episodeId'))
    dev=set();dc=set();de=set()
    for r in rows(a.development,('TRAIN','VALIDATION_IID','VALIDATION_STRESS')):dev.add(fp(r));dc.add(r['metadata'].get('candidateId'));de.add(r['metadata'].get('episodeId'))
    out={'blind_rows_fingerprinted':len(bf),'historical_input_overlap':len(bf&hist),'development_input_overlap':len(bf&dev),'candidate_overlap':len(bc&(hc|dc)),'episode_overlap':len((be-{None})&((he|de)-{None})),'status':'PASS' if not (bf&hist or bf&dev or (bc&hc) or (bc&dc) or ((be-{None})&(he|de-{None}))) else 'FAIL'}
    json.dump(out,open(a.out,'w'),indent=2);print(json.dumps(out));return 0 if out['status']=='PASS' else 1
if __name__=='__main__': raise SystemExit(main())
