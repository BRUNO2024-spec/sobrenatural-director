# Multi-dimension action model

Every spatial action intent carries a `DimensionRef` through its target. The
catalog defaults to the current `DecisionContext` dimension; a different
dimension must be explicit. Provider/content resolution rejects unavailable or
cross-dimension targets without an explicit policy. No teleport or travel
action is executable in this phase.
