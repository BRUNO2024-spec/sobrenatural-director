# Learned Scorer V2 Java export

The offline export contract is a dependency-free JSON representation containing
the frozen configuration SHA, feature order, TRAIN normalization, OOV policy,
embedding tables, dense weights/biases, activation metadata, and golden
vectors. A future Java 8 scorer may implement these operations directly;
Minecraft does not load PyTorch, ONNX, or TorchScript. This phase defines the
format and does not integrate it into runtime.
