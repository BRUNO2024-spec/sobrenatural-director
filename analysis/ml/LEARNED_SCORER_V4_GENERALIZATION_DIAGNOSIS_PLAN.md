# V4 generalization-gap diagnosis plan

The consumed V3 blind corpus is diagnostic only and is permanently retired
from future evaluation. V3 TEST/HOLDOUT remain historical. This phase will
compare input distributions, compositions, support distances, candidate-set
structure, margins, collisions, teacher observability, and generator artifacts
without changing the frozen V3 model.

The V4 curriculum will be fresh and disjoint, with broad coverage retained and
explicit buckets for under-supported compositions, hard pairs, boundary cases,
NO_ACTION contexts, temporal episodes, and rare valid stress states. V4 uses
TRAIN, VALIDATION_IID, and VALIDATION_STRESS only; a separate future blind spec
is frozen now but is not generated or opened in this phase. No target, ID, seed,
split, post-decision field, or teacher shortcut may become an input.
