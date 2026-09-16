import math

def mean(xs): return sum(xs)/len(xs) if xs else float("nan")
def pearson(a,b):
    if len(a)<2: return float("nan")
    ma,mb=mean(a),mean(b); va=sum((x-ma)**2 for x in a); vb=sum((y-mb)**2 for y in b)
    return sum((x-ma)*(y-mb) for x,y in zip(a,b))/math.sqrt(va*vb) if va and vb else 0.0
def ranks(xs):
    order=sorted(range(len(xs)),key=lambda i:(xs[i],i)); out=[0.0]*len(xs)
    for r,i in enumerate(order): out[i]=r+1
    return out
def spearman(a,b): return pearson(ranks(a),ranks(b))
def regression(pred,target):
    e=[x-y for x,y in zip(pred,target)]
    return {"mse":mean([x*x for x in e]),"rmse":math.sqrt(mean([x*x for x in e])),"mae":mean([abs(x) for x in e]),"pearson":pearson(pred,target),"spearman":spearman(pred,target)}
def group_metrics(groups):
    pairs=correct=0; reciprocal=[]; ndcgs=[]; regrets=[]; pred_quality=[]
    for rows in groups.values():
        if not rows: continue
        ordered=sorted(rows,key=lambda r:(-r["prediction"],r["candidateId"])); best=max(r["target"] for r in rows)
        top=ordered[0]; pred_quality.append(top["target"]); regrets.append(best-top["target"])
        rank=next((i+1 for i,r in enumerate(ordered) if r["target"]==best),len(rows)); reciprocal.append(1.0/rank)
        for x in rows:
            for y in rows:
                if x["target"]==y["target"]: continue
                pairs+=1; correct += (x["prediction"] > y["prediction"]) == (x["target"] > y["target"])
        def dcg(rs): return sum((2**r["target"]-1)/math.log2(i+2) for i,r in enumerate(rs))
        ndcgs.append(dcg(ordered)/dcg(sorted(rows,key=lambda r:-r["target"])) if dcg(sorted(rows,key=lambda r:-r["target"])) else 1.0)
    return {"top1_candidate_agreement":mean([1.0 if r==max(x["target"] for x in groups[g]) else 0.0 for g,r in ((g,max(v,key=lambda x:x["prediction"])["target"]) for g,v in groups.items())]),"pairwise_accuracy":correct/pairs if pairs else float("nan"),"mrr":mean(reciprocal),"ndcg_at_k":mean(ndcgs),"mean_regret":mean(regrets),"predicted_best_target_quality":mean(pred_quality)}
