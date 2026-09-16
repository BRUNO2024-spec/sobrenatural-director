"""Create deterministic V4 golden vectors from development data only."""
import argparse,json,hashlib,torch
from v4_preprocess import encode
def main():
 p=argparse.ArgumentParser();p.add_argument('--cache',required=True);p.add_argument('--checkpoint',required=True);p.add_argument('--config-sha',required=True);p.add_argument('--out',required=True);a=p.parse_args();d=torch.load(a.cache,map_location='cpu',weights_only=False);ck=torch.load(a.checkpoint,map_location='cpu',weights_only=False);from v2_model import LearnedScorerV2;m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=tuple(ck['config']['hidden']));m.load_state_dict(ck['model_state_dict']);m.eval();rows=[]
 for i in range(64):
  x=d['validation_stress_numeric'][i:i+1];c=d['validation_stress_categorical'][i:i+1]
  with torch.no_grad():y=float(m(x,c)[0])
  rows.append({'schema':'DIRECTOR_EXPERIENCE_FEATURES_V4','frozen_config_sha256':a.config_sha,'numeric_normalized':x[0].tolist(),'categorical_ids':c[0].tolist(),'expected_score':y})
 raw=''.join(json.dumps(x,sort_keys=True,separators=(',',':'))+'\n' for x in rows).encode();open(a.out,'wb').write(raw);print(json.dumps({'count':len(rows),'sha256':hashlib.sha256(raw).hexdigest()}))
if __name__=='__main__':main()
