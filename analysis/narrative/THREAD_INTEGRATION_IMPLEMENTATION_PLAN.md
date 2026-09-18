# Thread integration implementation plan

Implemented in this delta:

1. `PersistentNarrativeThread` stores bounded opaque `situationIds`.
2. `linkSituation` is idempotent and refuses no state transition by itself.
3. `NarrativeThreadManager.linkSituation` updates only narrative membership;
   world ownership remains in provider/control journals.
4. `SituationPersistenceCoordinator` saves the situation before mutation and
   links the thread after the durable record exists.
5. Existing thread NBT remains readable because missing membership defaults to
   an empty list.

Still required for a full closure: policy-driven suspend/resume, explicit
multi-dimension continuation, and reconciliation of provider/action journals
after a second Forge process starts on the same world.
