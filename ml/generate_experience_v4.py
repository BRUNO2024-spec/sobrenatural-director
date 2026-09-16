"""Fresh V4 curriculum corpus generator using a disjoint seed/ID namespace."""
import argparse,hashlib,json,time
from pathlib import Path
from generate_experience_v3 import state,candidates,quality
SCHEMA='DIRECTOR_EXPERIENCE_FEATURES_V4'
BUCKETS=[('TEMPORAL_EPISODIC',.10),('RARE_STRESS',.05),('UNDER_SUPPORTED_COMPOSITIONS',.15),('HARD_PAIR_STATES',.10),('BOUNDARY_STATES',.10),('NO_ACTION_CONTEXTS',.10),('BROAD_IID_COVERAGE',.40)]
def qualifies(bucket,s,cs,ep):
 q=sorted((quality(s,c) for c in cs),reverse=True);margin=q[0]-q[1]
 no=quality(s,'no-action');best=max(q)
 if bucket=='UNDER_SUPPORTED_COMPOSITIONS':return s['environment'] in ('VANILLA_VILLAGE','UNKNOWN','WILDERNESS') and s['providerMode'] in ('THREAT_ONLY','NONE')
 if bucket=='BOUNDARY_STATES':return margin<=.02
 if bucket=='HARD_PAIR_STATES':return .02<margin<=.08
 if bucket=='NO_ACTION_CONTEXTS':return (not s['safetyKnown']) or s['cooldownActive'] or s['recoveryNeed']>.7 or s['fatigue']>.7
 if bucket=='TEMPORAL_EPISODIC':return ep is not None
 if bucket=='RARE_STRESS':return (not s['safetyKnown']) or (s['fatigue']>.8 and s['recoveryNeed']>.7)
 return ep is None
def main():
 p=argparse.ArgumentParser();p.add_argument('--states',type=int,default=120000);p.add_argument('--seed',type=int,default=909090);p.add_argument('--out',required=True);p.add_argument('--run-id',required=True);a=p.parse_args();root=Path(a.out);splits={x:root/x for x in ('TRAIN','VALIDATION_IID','VALIDATION_STRESS')};[x.mkdir(parents=True,exist_ok=True) for x in splits.values()];counts={x:{'rows':0,'states':0,'shards':0} for x in splits};writers={}
 def write(sp,row):
  idx=counts[sp]['rows']//10000
  if sp not in writers or writers[sp][0]!=idx:
   if sp in writers:writers[sp][1].close()
   writers[sp]=(idx,(splits[sp]/('part-%05d.jsonl'%idx)).open('w',encoding='utf8'));counts[sp]['shards']+=1
  writers[sp][1].write(json.dumps(row,sort_keys=True,separators=(',',':'))+'\n');counts[sp]['rows']+=1
 quotas={k:int(a.states*pct) for k,pct in BUCKETS};quotas['BROAD_IID_COVERAGE']=a.states-sum(int(a.states*pct) for k,pct in BUCKETS if k!='BROAD_IID_COVERAGE');accepted=[];used=set();cursor=0
 while len(accepted)<a.states:
  ep=cursor//10 if cursor<20000 else None;step=cursor%10 if ep is not None else 0;s=state(cursor,a.seed,ep,step);cs=candidates(s)
  for b,_ in BUCKETS:
   if quotas[b] and qualifies(b,s,cs,ep):accepted.append((cursor,ep,step,s,b));quotas[b]-=1;used.add(cursor);break
  cursor+=1
  if cursor > max(1000000, a.states*20):
   raise RuntimeError('curriculum quotas are not attainable within bounded fresh namespace')
 for i,ep,step,s,b in accepted:
  split_key=ep if ep is not None else i;sp='VALIDATION_STRESS' if split_key%10==9 else 'VALIDATION_IID' if split_key%10==8 else 'TRAIN';cs=candidates(s);chosen=max(((quality(s,c),c) for c in cs),key=lambda z:(z[0],z[1]))[1];counts[sp]['states']+=1
  for c in cs:
   cf={'candidateKind':c,'intent':{'ambient':'AMBIENT_HINT','continuation':'MYSTERY_PAYOFF','threat':'BOSS_ENCOUNTER','recovery':'RECOVERY','exploration':'LOOT_DISCOVERY','no-action':'NO_ACTION'}[c],'safety':'NON_DESTRUCTIVE' if c!='threat' else 'MAJOR','provider':'threat' if c=='threat' else 'structure' if c in ('ambient','continuation','exploration') else 'none','baseUtility':{'ambient':45,'continuation':60,'threat':70,'recovery':55,'exploration':50,'no-action':35}[c]}
   write(sp,{'metadata':{'schemaVersion':SCHEMA,'stateId':'V4-S-%07d'%i,'candidateId':'V4-%s-%07d'%(c,i),'episodeId':'V4-E-%05d'%ep if ep is not None else None,'step':step if ep is not None else None,'split':sp,'trainingAllowed':sp=='TRAIN','curriculumBucket':b},'stateFeatures':s,'candidateFeatures':cf,'supervision':{'teacherQuality':quality(s,c),'teacherChosen':c==chosen,'targetProvenance':'DETERMINISTIC_BENCHMARK_TEACHER'}})
 for _,w in writers.values():w.close()
 shards=[];hashlines=[]
 for f in sorted(root.glob('*/part-*.jsonl')):
  z=f.read_bytes();h=hashlib.sha256(z).hexdigest();shards.append({'path':str(f.relative_to(root)),'rows':len(z.splitlines()),'bytes':len(z),'sha256':h});hashlines.append(h+'  '+str(f.relative_to(root)))
 (root/'hashes.sha256').write_text('\n'.join(hashlines)+'\n');man={'datasetName':'DIRECTOR_EXPERIENCE_DATASET_V4','datasetVersion':'DIRECTOR_EXPERIENCE_DATASET_V4','runId':a.run_id,'globalSeed':a.seed,'featureSchema':SCHEMA,'teacherProvenance':'DETERMINISTIC_BENCHMARK_TEACHER','teacherFingerprint':'3904dfd83c65bbf8b16d4aa2fd2fdfbac74711741cfa26d06b0686a8b16dc15d','splits':counts,'curriculumBuckets':{'BROAD_IID_COVERAGE':.40,'UNDER_SUPPORTED_COMPOSITIONS':.15,'HARD_PAIR_STATES':.10,'BOUNDARY_STATES':.10,'NO_ACTION_CONTEXTS':.10,'TEMPORAL_EPISODIC':.10,'RARE_STRESS':.05},'totalStateGroups':a.states,'totalCandidateRows':sum(x['rows'] for x in counts.values()),'sequenceEpisodes':2000,'sequenceStates':20000,'shards':shards,'totalBytes':sum(x['bytes'] for x in shards),'createdUtc':time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime())};(root/'manifest.json').write_text(json.dumps(man,sort_keys=True,indent=2)+'\n');print(json.dumps(man,sort_keys=True))
if __name__=='__main__':main()
