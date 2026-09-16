# Java inference plan (future, offline design only)

The frozen model is small enough for a dependency-free Java implementation:
five embedding tables, one dense layer, ReLU, dropout disabled at inference,
and one output affine layer. Python exports the exact feature order, TRAIN
normalization means/stds, categorical vocabularies, embedding arrays, weights
and biases in a versioned binary/JSON metadata envelope.

Inference operations are: map unknown categories to index 0; normalize the
seven numeric values; concatenate embeddings; dense multiply/add; ReLU; dense
multiply/add. No PyTorch or TorchScript is proposed for Minecraft.

Parity will use deterministic golden vectors containing raw state/candidate
input, encoded input, expected score and frozen config SHA. Java and Python
must agree within a documented float tolerance on CPU. Serialization must
reject schema/config/order/hash mismatches, NaN/Inf and empty candidate sets.
The future runtime boundary still receives only deterministic legal candidates;
all hard safety and authorization checks remain external.

Expected overhead is one 1,029-parameter forward pass per candidate. The
current remote CPU measurement was p50 0.783 ms, p95 0.896 ms and p99 0.931 ms
for the complete 3,356-row batch, not a production tick guarantee.
