# V2 Java inference closure

The export contains TRAIN-normalized numeric inputs, OOV index zero,
categorical embedding tables, concatenation order, ReLU dense layers, and the
final scalar output. The test-only Java 8 harness in
`tools/v2_java_parity/ScorerParity.java` reads the dependency-free weight text
export and the 64 TRAIN/VALIDATION golden vectors. It reproduced all vectors
with maximum absolute error `7.08e-8`. No Director runtime class imports this
harness or export.
