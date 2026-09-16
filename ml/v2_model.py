import torch
from torch import nn

class LearnedScorerV2(nn.Module):
    def __init__(self, numeric_dim, vocab_sizes, embedding_dim=4, hidden=(64,32), dropout=0.0, activation="relu"):
        super().__init__(); self.embeddings=nn.ModuleList([nn.Embedding(n,embedding_dim) for n in vocab_sizes]); act=nn.GELU if activation.lower()=="gelu" else nn.ReLU
        dims=[numeric_dim+embedding_dim*len(vocab_sizes)]+list(hidden); layers=[]
        for a,b in zip(dims,dims[1:]): layers += [nn.Linear(a,b),act(),nn.Dropout(dropout)]
        layers.append(nn.Linear(dims[-1],1)); self.net=nn.Sequential(*layers)
    def forward(self,numeric,categorical):
        if numeric.size(0)==0: raise ValueError("cannot score empty candidate set")
        return self.net(torch.cat([numeric]+[e(categorical[:,i]) for i,e in enumerate(self.embeddings)],1)).squeeze(1)
