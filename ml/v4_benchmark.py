"""Frozen V4 CPU/GPU latency and deterministic parity probe."""
import argparse,json,time
import torch
from v2_model import LearnedScorerV2
def main():
 p=argparse.ArgumentParser();p.add_argument('--cache',required=True);p.add_argument('--checkpoint',required=True);p.add_argument('--out',required=True);a=p.parse_args();d=torch.load(a.cache,map_location='cpu',weights_only=False);ck=torch.load(a.checkpoint,map_location='cpu',weights_only=False);m=LearnedScorerV2(d['numeric_dim'],d['vocab_sizes'],hidden=tuple(ck['config']['hidden']));m.load_state_dict(ck['model_state_dict']);m.eval();x=d['validation_iid_numeric'][:16];c=d['validation_iid_categorical'][:16]
 def measure(n):
  for _ in range(20):
   with torch.no_grad():m(x[:n],c[:n])
  ts=[]
  for _ in range(100):
   t=time.perf_counter();
   with torch.no_grad():m(x[:n],c[:n])
   ts.append((time.perf_counter()-t)*1000)
  ts.sort();return {'p50':ts[49],'p95':ts[94],'p99':ts[99]}
 out={'parameterCount':sum(x.numel() for x in m.parameters()),'cpu_single':measure(1),'cpu_typical':measure(min(6,len(x))),'cpu_large':measure(len(x))}
 if torch.cuda.is_available():
  g=m.cuda();gx=x.cuda();gc=c.cuda();
  for _ in range(20):g(gx,gc)
  torch.cuda.synchronize(); vals=[]
  for _ in range(100):
   t=time.perf_counter();g(gx,gc);torch.cuda.synchronize();vals.append((time.perf_counter()-t)*1000)
  out['gpu_16_p50']=sorted(vals)[49];out['cpu_gpu_max_abs_diff']=float((m(x,c)-g(gx,gc).cpu()).abs().max())
 json.dump(out,open(a.out,'w'),indent=2);print(json.dumps(out))
if __name__=='__main__':main()
