"""Deterministic, dependency-free inference export (weights are not Git data)."""
import argparse, json, hashlib
import torch
from v2_model import LearnedScorerV2
def main():
    p=argparse.ArgumentParser(); p.add_argument('--checkpoint',required=True); p.add_argument('--config',required=True); p.add_argument('--out',required=True); a=p.parse_args(); ck=torch.load(a.checkpoint,map_location='cpu',weights_only=False); cfg=json.load(open(a.config)); c=ck['config']; hidden=tuple([c['hidden']]+[int(x) for x in c['layers'].split(',') if x]); m=LearnedScorerV2(len(cfg['numeric_feature_order']),[len(cfg['vocabs'][x]) for x in cfg['categorical_feature_order']],hidden=hidden,dropout=c['dropout'],activation=c['activation']); m.load_state_dict(ck['model_state_dict']); layers=[]
    for layer in m.net:
        if isinstance(layer,torch.nn.Linear): layers.append({'type':'linear','in':layer.in_features,'out':layer.out_features,'weight':layer.weight.detach().tolist(),'bias':layer.bias.detach().tolist()})
        elif isinstance(layer,torch.nn.ReLU): layers.append({'type':'relu'})
    o={'format_version':1,'frozen_config_sha256':cfg['frozen_config_sha256'],'feature_schema_sha256':cfg['feature_schema_sha256'],'dataset_manifest_sha256':cfg['dataset_manifest_sha256'],'teacher_fingerprint':cfg['teacher_fingerprint'],'numeric_feature_order':cfg['numeric_feature_order'],'normalization':cfg['normalization'],'categorical_feature_order':cfg['categorical_feature_order'],'vocabs':cfg['vocabs'],'oov_index':0,'embedding_dim':4,'embeddings':[e.weight.detach().tolist() for e in m.embeddings],'layers':layers}
    raw=json.dumps(o,sort_keys=True,separators=(',',':')).encode(); open(a.out,'wb').write(raw)
    with open(a.out+'.weights','w') as f:
        f.write('EMBEDDINGS %d %d\n' % (len(m.embeddings),4))
        for e in m.embeddings:
            w=e.weight.detach().tolist(); f.write('E %d\n' % len(w)); f.write(' '.join(str(v) for row in w for v in row)+'\n')
        linear=[x for x in m.net if isinstance(x,torch.nn.Linear)]; f.write('LAYERS %d\n' % len(linear))
        for l in linear: f.write('L %d %d\n' % (l.in_features,l.out_features)); f.write(' '.join(str(v) for v in l.weight.detach().reshape(-1).tolist())+'\n'); f.write(' '.join(str(v) for v in l.bias.detach().tolist())+'\n')
    print(json.dumps({'sha256':hashlib.sha256(raw).hexdigest(),'bytes':len(raw),'weights_path':a.out+'.weights'}))
if __name__=='__main__': main()
