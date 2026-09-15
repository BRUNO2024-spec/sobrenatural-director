# Scenario Catalog

V1 deterministically generates 600 single-step scenarios and 60 sequence families of 10 decisions. Families cover: `NO_ACTION_REQUIRED`, `MULTIPLE_VALID_CHOICES`, continuation, novelty pressure, recovery, building tension, player base, wilderness, missing/returned providers, multi-provider, dimension isolation, thread competition/dormancy, repetition traps, false high intensity, low-impact continuation and provider-fit contrast.

Each scenario carries a stable id, seed, family, dimension, environment classification, provider availability, continuation flag and recent intensity state. Scenarios are generated rather than hand-copied, and every decision emits JSONL detail and feature records.

Coverage includes all current environment classifications, Overworld/Nether identities, provider present/absent, no-action, continuation/new-thread proxies, and low/high safety candidates. Gaps are explicit: this V1 is semantic simulation, not physical Minecraft outcome validation.
