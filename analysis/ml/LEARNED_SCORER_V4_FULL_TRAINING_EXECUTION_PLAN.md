# Learned Scorer V4 full-training execution plan

## Locks before training

The development corpus is fixed at 120,000 state groups and 629,511 rows.
Normalization and categorical vocabularies will be fit from TRAIN only;
VALIDATION_IID and VALIDATION_STRESS are development-only selection data. The
future blind specification is verified by hash and its dataset must not exist
before a model-freeze commit.

## Compact study

Run mean and linear diagnostics, the 3,625-parameter V3-compatible MLP, and a
small predeclared capacity/objective matrix. Compare pairwise against one
hybrid pairwise-plus-regression objective; no Cross Network search is reopened.
Use seeds 7/17/27 for finalists. The frozen selection metric is
`0.6 * IID_REGRET + 0.4 * STRESS_REGRET`, with seed variance as a tie-breaker.
Early stopping reads only the two validation splits.

## Freeze and blind protocol

Freeze one architecture/configuration and commit it before generating the
fresh blind. Final training uses a frozen epoch count, then produces an
atomic checkpoint with all provenance, optimizer, and RNG state. Only after
the freeze commit will the separate test/holdout corpus be generated and
evaluated. Any post-blind model change invalidates the gate.

## Safety and evidence

All experiments remain offline. No runtime Java/Minecraft integration,
PyTorch runtime dependency, Shadow Mode, hard-safety learning, or protected
world access is permitted. A strong synthetic result still does not establish
real gameplay generalization; readiness is evaluated separately and may
remain `INSUFFICIENT_EVIDENCE`.
