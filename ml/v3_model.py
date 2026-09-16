"""Small Java-exportable V3 representation experiments."""
import torch
from torch import nn

class Encoder(nn.Module):
    def __init__(self,numeric_dim,vocab_sizes,embedding_dim=4):
        super().__init__(); self.embeddings=nn.ModuleList([nn.Embedding(n,embedding_dim) for n in vocab_sizes]); self.dim=numeric_dim+embedding_dim*len(vocab_sizes)
    def forward(self,n,c): return torch.cat([n]+[e(c[:,i]) for i,e in enumerate(self.embeddings)],1)

def semantic_crosses(n,c):
    # X01/X02/X06 are numeric products; X07/X08/X09 are candidate-conditioned.
    x=[n[:,0]*n[:,6],n[:,0]*n[:,4],n[:,11]*n[:,16]]
    one=torch.nn.functional.one_hot(c[:,4],num_classes=7).float()
    for j in (2,3,5): x.extend((n[:,j:j+1]*one).unbind(1))
    return torch.stack(x,1)

class ExplicitCrossScorer(nn.Module):
    def __init__(self,numeric_dim,vocab_sizes,hidden=64,embedding_dim=4,dropout=0.0):
        super().__init__(); self.encoder=Encoder(numeric_dim,vocab_sizes,embedding_dim); self.cross_dim=24; self.net=nn.Sequential(nn.Linear(self.encoder.dim+self.cross_dim,hidden),nn.ReLU(),nn.Dropout(dropout),nn.Linear(hidden,1))
    def forward(self,n,c):
        if n.size(0)==0: raise ValueError('cannot score an empty candidate set')
        return self.net(torch.cat([self.encoder(n,c),semantic_crosses(n,c)],1)).squeeze(1)

class CrossNetworkScorer(nn.Module):
    def __init__(self,numeric_dim,vocab_sizes,hidden=64,depth=1,embedding_dim=4,dropout=0.0):
        super().__init__(); self.encoder=Encoder(numeric_dim,vocab_sizes,embedding_dim); d=self.encoder.dim; self.weights=nn.ParameterList([nn.Parameter(torch.zeros(d)) for _ in range(depth)]); self.biases=nn.ParameterList([nn.Parameter(torch.zeros(d)) for _ in range(depth)]); self.head=nn.Sequential(nn.Linear(d,hidden),nn.ReLU(),nn.Dropout(dropout),nn.Linear(hidden,1))
    def forward(self,n,c):
        if n.size(0)==0: raise ValueError('cannot score an empty candidate set')
        x0=self.encoder(n,c); x=x0
        for w,b in zip(self.weights,self.biases): x=x0*(x@w).unsqueeze(1)+b+x
        return self.head(x).squeeze(1)
