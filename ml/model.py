import torch
from torch import nn

class LearnedScorerV1(nn.Module):
    def __init__(self, numeric_dim, vocab_sizes, embedding_dim=4, hidden=32, dropout=0.0):
        super().__init__()
        self.embeddings = nn.ModuleList([nn.Embedding(n, embedding_dim) for n in vocab_sizes])
        self.net = nn.Sequential(nn.Linear(numeric_dim + embedding_dim*len(vocab_sizes), hidden), nn.ReLU(), nn.Dropout(dropout), nn.Linear(hidden, 1))
    def forward(self, numeric, categorical):
        if numeric.size(0) == 0: raise ValueError("cannot score an empty candidate set")
        parts = [e(categorical[:, i]) for i,e in enumerate(self.embeddings)]
        return self.net(torch.cat([numeric] + parts, dim=1)).squeeze(1)
