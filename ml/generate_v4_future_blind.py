"""Generate the locked V4 blind only after model freeze; never used for tuning."""
import argparse, hashlib, json, time
from pathlib import Path
from generate_experience_v3 import state, candidates, quality

def main():
    p=argparse.ArgumentParser();p.add_argument('--spec',required=True);p.add_argument('--frozen-config',required=True);p.add_argument('--out',required=True);p.add_argument('--run-id',required=True);a=p.parse_args()
    spec=json.load(open(a.spec)); frozen=json.load(open(a.frozen_config));
    if not spec.get('generateOnlyAfterModelFreeze') or frozen.get('configVersion')!='LEARNED_SCORER_V4_FROZEN_V1': raise SystemExit('model freeze guard failed')
    root=Path(a.out); splits={'TEST':root/'TEST','HOLDOUT':root/'HOLDOUT'}; [x.mkdir(parents=True,exist_ok=True) for x in splits.values()]; counts={x:{'states':0,'rows':0,'shards':0} for x in splits}; writers={}
    def write(sp,row):
        idx=counts[sp]['rows']//10000
        if sp not in writers or writers[sp][0]!=idx:
            if sp in writers: writers[sp][1].close()
            writers[sp]=(idx,(splits[sp]/('part-%05d.jsonl'%idx)).open('w',encoding='utf8'));counts[sp]['shards']+=1
        writers[sp][1].write(json.dumps(row,sort_keys=True,separators=(',',':'))+'\n');counts[sp]['rows']+=1
    total=spec['testStateGroups']+spec['holdoutStateGroups']; seed=spec['globalSeed']; start=time.time()
    for j in range(total):
        i=j+1000000; sp='TEST' if j<spec['testStateGroups'] else 'HOLDOUT'; ep=j//10 if j<20000 else None; step=j%10 if ep is not None else 0; s=state(i,seed,ep,step); cs=candidates(s); chosen=max(((quality(s,c),c) for c in cs),key=lambda z:(z[0],z[1]))[1]; counts[sp]['states']+=1
        for c in cs:
            cf={'candidateKind':c,'intent':{'ambient':'AMBIENT_HINT','continuation':'MYSTERY_PAYOFF','threat':'BOSS_ENCOUNTER','recovery':'RECOVERY','exploration':'LOOT_DISCOVERY','no-action':'NO_ACTION'}[c],'safety':'NON_DESTRUCTIVE' if c!='threat' else 'MAJOR','provider':'threat' if c=='threat' else 'structure' if c in ('ambient','continuation','exploration') else 'none','baseUtility':{'ambient':45,'continuation':60,'threat':70,'recovery':55,'exploration':50,'no-action':35}[c]}
            write(sp,{'metadata':{'schemaVersion':'DIRECTOR_EXPERIENCE_FEATURES_V4','stateId':f"{spec['stateIdPrefix']}{j:07d}",'candidateId':f"{spec['datasetName']}-{c}-{j:07d}",'episodeId':f"{spec['episodeIdPrefix']}{ep:05d}" if ep is not None else None,'step':step if ep is not None else None,'split':sp,'trainingAllowed':False},'stateFeatures':s,'candidateFeatures':cf,'supervision':{'teacherQuality':quality(s,c),'teacherChosen':c==chosen,'targetProvenance':'DETERMINISTIC_BENCHMARK_TEACHER'}})
    for _,w in writers.values():w.close()
    shards=[]; lines=[]
    for f in sorted(root.glob('*/part-*.jsonl')):
        b=f.read_bytes(); h=hashlib.sha256(b).hexdigest(); item={'path':str(f.relative_to(root)),'rows':len(b.splitlines()),'bytes':len(b),'sha256':h};shards.append(item);lines.append(h+'  '+item['path'])
    (root/'hashes.sha256').write_text('\n'.join(lines)+'\n'); manifest={'datasetName':spec['datasetName'],'datasetVersion':'DIRECTOR_V4_BLIND_EVAL_V1','runId':a.run_id,'generatorVersion':spec['generatorVersion'],'featureSchema':spec['featureSchema'],'teacherFingerprint':spec['teacherFingerprint'],'specSha256':hashlib.sha256(Path(a.spec).read_bytes()).hexdigest(),'frozenConfigSha256':hashlib.sha256(Path(a.frozen_config).read_bytes()).hexdigest(),'globalSeed':seed,'splits':counts,'totalStateGroups':total,'totalCandidateRows':sum(x['rows'] for x in counts.values()),'shards':shards,'totalBytes':sum(x['bytes'] for x in shards),'generatedAfterModelFreeze':True,'elapsedSeconds':time.time()-start,'createdUtc':time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime())};(root/'manifest.json').write_text(json.dumps(manifest,sort_keys=True,indent=2)+'\n');print(json.dumps(manifest,sort_keys=True))
if __name__=='__main__':main()
