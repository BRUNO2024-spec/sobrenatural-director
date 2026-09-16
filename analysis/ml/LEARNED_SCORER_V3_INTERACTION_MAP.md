# V3 real-feature interaction map

| ID | Feature A | Feature B | Type | Priority | Rationale |
|---|---|---|---|---|---|
| X01 | tension | isolation | numeric×numeric | HIGH | threat/novelty pressure is context-sensitive to isolation |
| X02 | tension | healthRatio | numeric×numeric | HIGH | threat suitability depends on survivability |
| X03 | environment | candidateKind | state×candidate | HIGH | candidate utility varies by environment |
| X04 | providerMode | provider | state×candidate | HIGH | provider availability conditions provider candidates |
| X05 | narrativeState | intent | state×candidate | continuation depends on narrative phase |
| X06 | memoryPressure | repetitionCount | numeric×numeric | repetition and memory jointly affect pacing |
| X07 | fatigue | candidateKind | numeric×categorical | action choice depends on fatigue |
| X08 | recoveryNeed | candidateKind | numeric×categorical | recovery and NO_ACTION are context-sensitive |
| X09 | cooldownActive | candidateKind | numeric×categorical | cooldown changes action suitability |
| X10 | episodeStep | narrativeState | numeric×categorical | sequence pacing interacts with narrative state |

The real state numeric inputs are the 19 features in the frozen V3 order;
state categoricals are family/environment/providerMode/narrativeState and
candidate categoricals are candidateKind/intent/safety/provider. All are
pre-decision. IDs, split, seeds, targets, and post-decision fields are
rejected. X01/X02/X06 are numeric products; X03/X04/X05/X07/X08/X09/X10 are
bounded numeric×candidate conditioning. Arbitrary pair buckets and unbounded
cross products are rejected for Java simplicity and OOV safety.
