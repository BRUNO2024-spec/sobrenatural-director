# V2 evidence and engineering closure plan

The V3 manifest, split isolation, frozen model, TEST and HOLDOUT are already
locked. The frozen configuration is immutable and its required SHA is
`45b174fbe1ad166f4dd42a486d0dc5ec3cd2beaa72b861c3a2c1e31f133d9cd4`.

This closure performs no tuning, fitting, architecture/loss/LR/epoch changes,
or model selection. TEST and HOLDOUT are read-only post-freeze analysis only;
diagnostic ablations use TRAIN/VALIDATION and cannot produce a new model.

Pending evidence is closed through checkpoint integrity and two-process resume,
feature-mask ablations, held-out slice/NO_ACTION/sequence/boundary analysis,
CPU latency, CPU/GPU parity, deterministic Java-oriented export, golden vectors,
robustness checks, and final tests. Shadow Mode remains disabled and hard safety
remains external and deterministic.
