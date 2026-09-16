"""Generate the post-freeze blind corpus; never writes to the V3 corpus."""
import argparse,hashlib,json,time
from pathlib import Path
from generate_experience_v3 import state,candidates,quality,sha_obj
SCHEMA='DIRECTOR_EXPERIENCE_FEATURES_V3'
def main():
 p=argparse.ArgumentParser();p.add_argument('--out',required=True);p.add_argument('--seed',type=int,default=424242);p.add_argument('--run-id',required=True);p.add_argument('--states',type=int,default=30000);a=p.parse_args();root=Path(a.out); splits={'TEST':root/'TEST','HOLDOUT':root/'HOLDOUT'}
 for x in splits.values():x.mkdir(parents=True,exist_ok=True)
 counts={k:{'rows':0,'states':0,'shards':0} for k in splits}; writers={}
 def write(sp,row):
  idx=counts[sp]['rows']//10000
  if sp not in writers or writers[sp][0]!=idx:
   if sp in writers:writers[sp][1].close()
   writers[sp]=(idx,(splits[sp]/('part-%05d.jsonl'%idx)).open('w'));counts[sp]['shards']+=1
  writers[sp][1].write(json.dumps(row,sort_keys=True,separators=(',',':'))+'\n');counts[sp]['rows']+=1
 for i in range(a.states):
  sp='TEST' if i<a.states//2 else 'HOLDOUT'; s=state(i,a.seed); chosen=max(((quality(s,c),c) for c in candidates(s)),key=lambda z:(z[0],z[1]))[1]
  for c in candidates(s):
   cf={'candidateKind':c,'intent':{'ambient':'AMBIENT_HINT','continuation':'MYSTERY_PAYOFF','threat':'BOSS_ENCOUNTER','recovery':'RECOVERY','exploration':'LOOT_DISCOVERY','no-action':'NO_ACTION'}[c],'safety':'NON_DESTRUCTIVE' if c!='threat' else 'MAJOR','provider':'threat' if c=='threat' else 'structure' if c in ('ambient','continuation','exploration') else 'none','baseUtility':{'ambient':45,'continuation':60,'threat':70,'recovery':55,'exploration':50,'no-action':35}[c]}
   write(sp,{'metadata':{'schemaVersion':SCHEMA,'stateId':'CROSS-EVAL-V1-S-%07d'%i,'candidateId':'%s-%07d'%(c,i),'episodeId':'CROSS-EVAL-V1-E-%05d'%(i//10),'step':i%10,'split':sp,'trainingAllowed':False},'stateFeatures':s,'candidateFeatures':cf,'supervision':{'teacherQuality':quality(s,c),'teacherChosen':c==chosen,'targetProvenance':'DETERMINISTIC_BENCHMARK_TEACHER'}})
 for _,w in writers.values():w.close()
 shards=[];hashes=[]
 for f in sorted(root.glob('*/part-*.jsonl')):
  b=f.read_bytes();h=hashlib.sha256(b).hexdigest();shards.append({'path':str(f.relative_to(root)),'rows':len(b.splitlines()),'bytes':len(b),'sha256':h});hashes.append(h+'  '+str(f.relative_to(root)))
 (root/'hashes.sha256').write_text('\n'.join(hashes)+'\n');man={'datasetName':'DIRECTOR_CROSS_EVAL_V1','runId':a.run_id,'seed':a.seed,'stateIdPrefix':'CROSS-EVAL-V1-S-','episodeIdPrefix':'CROSS-EVAL-V1-E-','featureSchema':SCHEMA,'teacherFingerprint':'3904dfd83c65bbf8b16d4aa2fd2fdfbac74711741cfa26d06b0686a8b16dc15d','splits':counts,'totalStateGroups':a.states,'totalCandidateRows':sum(x['rows'] for x in counts.values()),'shards':shards,'createdUtc':time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime())};(root/'manifest.json').write_text(json.dumps(man,sort_keys=True,indent=2)+'\n');print(json.dumps(man,sort_keys=True))
if __name__=='__main__':main()
