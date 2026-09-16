"""Streaming deterministic V3 experience generator; no Minecraft/world access."""
import argparse, hashlib, json, math, os, subprocess, time
from pathlib import Path

SCHEMA="DIRECTOR_EXPERIENCE_FEATURES_V3"; VERSION="director-experience-v3-generator-1"
FAMILIES=["NO_ACTION_REQUIRED","MULTIPLE_VALID_CHOICES","CONTINUATION_OPPORTUNITY","NOVELTY_PRESSURE","RECOVERY_REQUIRED","BUILDING_TENSION","PLAYER_BASE_NEARBY","WILDERNESS","PROVIDER_MISSING","PROVIDER_RETURNED","MULTI_PROVIDER","DIMENSION_ISOLATION","THREAD_COMPETITION","THREAD_DORMANCY_RESUME","REPETITION_TRAP","FALSE_HIGH_INTENSITY_TEMPTATION","LOW_IMPACT_CONTINUATION","PROVIDER_FIT_CONTRAST"]
ENVS=["UNKNOWN","WILDERNESS","PLAYER_MODIFIED_AREA","TEMPORARY_CAMP","ESTABLISHED_PLAYER_AREA","VANILLA_VILLAGE"]
PROVIDERS=["NONE","STRUCTURE_ONLY","ACTOR_ONLY","THREAT_ONLY","ALL_AVAILABLE"]
NARRATIVE=["NONE","DORMANT","ACTIVE","SUSPENDED"]
CANDIDATES=["ambient","continuation","threat","recovery","exploration","no-action"]

def halton(n, base):
    x=0.0; f=1.0/base
    while n: x+=f*(n%base); n//=base; f/=base
    return x
def sha_obj(x): return hashlib.sha256(json.dumps(x,sort_keys=True,separators=(",",":"),ensure_ascii=True).encode()).hexdigest()
def state(i, seed, episode=None, step=0):
    h=[halton(i+1+seed%97,b) for b in (2,3,5,7,11,13,17,19,23,29,31,37)]
    family=FAMILIES[i%len(FAMILIES)]; env=ENVS[(i*5+int(h[1]*len(ENVS)))%len(ENVS)]; pm=PROVIDERS[(i*7+int(h[2]*len(PROVIDERS)))%len(PROVIDERS)]
    features={"family":family,"environment":env,"tension":round(h[0],8),"pressure":round(h[1],8),"fatigue":round(h[2],8),"recoveryNeed":round(h[3],8),"healthRatio":round(.45+.55*h[4],8),"combatPower":round(h[5],8),"isolation":round(h[6],8),"underground":h[7]>.72,"observingSite":h[8]>.78,"safetyKnown":h[9]>.08,"eventConcurrency":int(h[10]*5),"providerMode":pm,"narrativeState":NARRATIVE[(i+int(h[11]*4))%4],"memoryPressure":int(h[4]*5),"cooldownActive":h[5]<.16}
    if episode is not None: features.update({"threadActive":step%4!=0,"threadAgeBucket":min(3,step//3),"recentHigh":max(0,2-step//4),"repetitionCount":step%3,"episodeStep":step})
    return features
def split_for(i, features, episode):
    if features["environment"]=="VANILLA_VILLAGE" and features["providerMode"]=="THREAT_ONLY": return "HOLDOUT"
    key=episode if episode is not None else i
    b=int(hashlib.sha256(("split:"+str(key)).encode()).hexdigest()[:8],16)%10
    return "TRAIN" if b<6 else "VALIDATION" if b<8 else "TEST" if b==8 else "HOLDOUT"
def candidates(s):
    out=["ambient","continuation","recovery","exploration","no-action"]
    if s["providerMode"] in ("THREAT_ONLY","ALL_AVAILABLE") and s["safetyKnown"] and s["environment"] not in ("ESTABLISHED_PLAYER_AREA","PLAYER_MODIFIED_AREA"): out.insert(2,"threat")
    return out
def quality(s,c):
    q={"ambient":.42+.18*(1-s["tension"])+.08*s["healthRatio"],"continuation":.42+.25*(s["narrativeState"] in ("ACTIVE","SUSPENDED"))+.12*s["memoryPressure"]/4,"threat":.35+.35*s["tension"]+.15*s["pressure"]+.08*s["combatPower"],"recovery":.38+.32*s["recoveryNeed"]+.15*s["fatigue"],"exploration":.4+.25*s["isolation"]+.1*(s["environment"] in ("WILDERNESS","UNKNOWN")),"no-action":.32+.3*(not s["safetyKnown"])+.2*s["cooldownActive"]+.1*(s["eventConcurrency"]==0)}[c]
    return round(max(0.0,min(1.0,q)),8)
def main():
    ap=argparse.ArgumentParser(); ap.add_argument("--states",type=int,required=True); ap.add_argument("--out",required=True); ap.add_argument("--seed",type=int,default=20260916); ap.add_argument("--run-id",required=True); args=ap.parse_args()
    root=Path(args.out); splits={x:root/x for x in ("TRAIN","VALIDATION","TEST","HOLDOUT")}; [p.mkdir(parents=True,exist_ok=True) for p in splits.values()]
    writers={}; counts={s:{"rows":0,"states":0,"shards":0} for s in splits}; fps={s:set() for s in splits}; shard_rows=10000
    def write(s,row):
        idx=counts[s]["rows"]//shard_rows
        if s not in writers or writers[s][0]!=idx:
            if s in writers: writers[s][1].close()
            path=splits[s]/("part-%05d.jsonl"%idx); writers[s]=(idx,path.open("w",encoding="utf-8")); counts[s]["shards"]+=1
        writers[s][1].write(json.dumps(row,sort_keys=True,separators=(",",":"))+"\n"); counts[s]["rows"]+=1
    episodes=1000; ep_steps=10
    for i in range(args.states):
        ep=i//ep_steps if i<episodes*ep_steps else None; step=i%ep_steps if ep is not None else 0; s=state(i,args.seed,ep,step); split=split_for(i,s,ep); sf=sha_obj(s); assert sf not in fps[split]; fps[split].add(sf); counts[split]["states"]+=1
        cs=candidates(s); scored=[(quality(s,c),c) for c in cs]; chosen=max(scored,key=lambda x:(x[0],x[1]))[1]
        for q,c in scored:
            cf={"candidateKind":c,"intent":{"ambient":"AMBIENT_HINT","continuation":"MYSTERY_PAYOFF","threat":"BOSS_ENCOUNTER","recovery":"RECOVERY","exploration":"LOOT_DISCOVERY","no-action":"NO_ACTION"}[c],"safety":"NON_DESTRUCTIVE" if c not in ("threat",) else "MAJOR","provider":"threat" if c=="threat" else "structure" if c in ("ambient","continuation","exploration") else "none","baseUtility":{"ambient":45,"continuation":60,"threat":70,"recovery":55,"exploration":50,"no-action":35}[c]}
            row={"metadata":{"schemaVersion":SCHEMA,"stateId":"V3-S-%07d"%i,"candidateId":"%s-%07d"%(c,i),"episodeId":"V3-E-%05d"%ep if ep is not None else None,"step":step if ep is not None else None,"split":split,"trainingAllowed":split=="TRAIN"},"stateFeatures":s,"candidateFeatures":cf,"supervision":{"teacherQuality":q,"teacherChosen":c==chosen,"targetProvenance":"DETERMINISTIC_BENCHMARK_TEACHER"}}
            write(split,row)
    for _,w in writers.values(): w.close()
    schema={"schemaVersion":SCHEMA,"metadataExcludedFromInput":["stateId","candidateId","episodeId","step","split","trainingAllowed"],"stateFeatures":sorted(state(0,args.seed).keys()),"candidateFeatures":["candidateKind","intent","safety","provider","baseUtility"],"supervision":["teacherQuality","teacherChosen"]}
    (root/"schema.json").write_text(json.dumps(schema,sort_keys=True,indent=2)+"\n"); config_path=Path("ml/configs/director_experience_v3_generation.json"); spec=json.loads(config_path.read_text()) if config_path.exists() else {"runId":args.run_id,"seed":args.seed,"states":args.states,"generatorVersion":VERSION,"targetProvenance":"DETERMINISTIC_BENCHMARK_TEACHER"}; (root/"generation-config.json").write_text(json.dumps(spec,sort_keys=True,indent=2)+"\n")
    shards=[]; hash_lines=[]
    for path in sorted(root.glob("*/part-*.jsonl")):
        data=path.read_bytes(); split=path.parent.name; item={"path":str(path.relative_to(root)),"split":split,"rows":sum(1 for _ in data.splitlines()),"bytes":len(data),"sha256":hashlib.sha256(data).hexdigest()}; shards.append(item); hash_lines.append(item["sha256"]+"  "+item["path"])
    (root/"hashes.sha256").write_text("\n".join(hash_lines)+"\n")
    try: source_commit=subprocess.check_output(["git","rev-parse","HEAD"],text=True).strip()
    except Exception: source_commit="unknown"
    try: teacher_fingerprint=hashlib.sha256(Path(__file__).read_bytes()).hexdigest()
    except Exception: teacher_fingerprint="unknown"
    manifest={"datasetName":"DIRECTOR_EXPERIENCE_DATASET_V3","datasetVersion":"DIRECTOR_EXPERIENCE_DATASET_V3","runId":args.run_id,"sourceCommit":source_commit,"featureSchema":SCHEMA,"generatorVersion":VERSION,"globalSeed":args.seed,"totalStateGroups":args.states,"totalCandidateRows":sum(x["rows"] for x in counts.values()),"splits":counts,"sequenceEpisodes":episodes,"sequenceStates":min(args.states,episodes*ep_steps),"teacherProvenance":"DETERMINISTIC_BENCHMARK_TEACHER","teacherImplementation":"ml/generate_experience_v3.py::quality","teacherFingerprint":teacher_fingerprint,"featureSchemaSha256":sha_obj(schema),"generationSpecSha256":sha_obj(spec),"shardCount":len(shards),"shards":shards,"totalBytes":sum(x["bytes"] for x in shards),"createdUtc":time.strftime("%Y-%m-%dT%H:%M:%SZ",time.gmtime())}
    (root/"manifest.json").write_text(json.dumps(manifest,sort_keys=True,indent=2)+"\n"); print(json.dumps(manifest,sort_keys=True))
if __name__=="__main__": main()
