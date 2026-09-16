"""Dependency-free JSON/weights export for the frozen V4 MLP."""
import argparse,hashlib,json
import torch
from v2_model import LearnedScorerV2
def main():
 p=argparse.ArgumentParser();p.add_argument('--checkpoint',required=True);p.add_argument('--config',required=True);p.add_argument('--out',required=True);a=p.parse_args();ck=torch.load(a.checkpoint,map_location='cpu',weights_only=False);cfg=json.load(open(a.config));m=LearnedScorerV2(len(cfg['numericFeatureOrder']),[len(cfg['vocabs'][x]) for x in cfg['categoricalFeatureOrder']],hidden=tuple(cfg['architecture']['hidden']),dropout=0);m.load_state_dict(ck['model_state_dict']);layers=[]
 for l in m.net:
  if isinstance(l,torch.nn.Linear):layers.append({'type':'linear','in':l.in_features,'out':l.out_features,'weight':l.weight.detach().tolist(),'bias':l.bias.detach().tolist()})
  elif isinstance(l,torch.nn.ReLU):layers.append({'type':'relu'})
 out={'formatVersion':1,'frozenConfigSha256':hashlib.sha256(open(a.config,'rb').read()).hexdigest(),'datasetManifestSha256':cfg['datasetManifestSha256'],'featureSchemaSha256':cfg['featureSchemaSha256'],'teacherFingerprint':cfg['teacherFingerprint'],'numericFeatureOrder':cfg['numericFeatureOrder'],'normalization':cfg['normalization'],'categoricalFeatureOrder':cfg['categoricalFeatureOrder'],'vocabs':cfg['vocabs'],'oovIndex':0,'embeddingDim':4,'embeddings':[e.weight.detach().tolist() for e in m.embeddings],'layers':layers};raw=json.dumps(out,sort_keys=True,separators=(',',':')).encode();open(a.out,'wb').write(raw);open(a.out+'.weights','w').write(raw.decode());print(json.dumps({'sha256':hashlib.sha256(raw).hexdigest(),'bytes':len(raw)}))
if __name__=='__main__':main()
