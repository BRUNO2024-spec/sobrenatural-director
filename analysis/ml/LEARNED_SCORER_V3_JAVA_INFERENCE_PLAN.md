# V3 Java inference plan

The selected V3 model is the frozen V2-style MLP with no explicit crosses.
Its future Java 8 implementation can reuse the proven V2 dependency-free
format: normalization, OOV index zero, embedding lookup, concatenation, ReLU,
and dense scalar output. Explicit-cross and Cross Network prototypes remain
offline-only and are not runtime dependencies. No Minecraft runtime code is
changed in this phase.
