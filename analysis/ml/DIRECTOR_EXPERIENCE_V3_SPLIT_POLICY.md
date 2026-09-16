# V3 split policy

Splits are assigned before candidate rows are written. Independent states use
a deterministic hash of the state index; episodic states use the episode key,
so all ten steps remain together. The approximate policy is 60% TRAIN, 20%
VALIDATION, 10% TEST and 10% HOLDOUT. HOLDOUT additionally reserves the valid
compositional `VANILLA_VILLAGE × THREAT_ONLY` state/provider combination; its
individual components occur elsewhere, but that pair is not assigned to
TRAIN. Candidate rows never split independently from their state.

TRAIN is the only fit source. VALIDATION is tuning-only. TEST and HOLDOUT are
locked artifacts and cannot be used for design/tuning in this foundation.
Metadata IDs, seeds, split labels and hashes are excluded from model inputs.
