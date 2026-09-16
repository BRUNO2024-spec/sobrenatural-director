# DIRECTOR Experience V3 generation report

```text
CLASSIFICATION=PASS_MASSIVE_DIRECTOR_EXPERIENCE_DATASET_V3_FOUNDATION
RUN_ID=experience-v3-20260916
```

The final corpus contains 60,000 unique state groups and 314,715 candidate
evaluations in 34 canonical JSONL shards. It occupies 266 MB on the persistent
VPS dataset root and was generated streaming in 11.7 seconds with approximately
69 MB peak RSS and zero OOM events.

Splits are group-aware: TRAIN 35,009 states/182,865 rows, VALIDATION 11,505/
60,055, TEST 5,641/29,443 and HOLDOUT 7,845/42,352. The first 1,000 episodes
contain 10,000 states and remain episode-grouped. HOLDOUT reserves the valid
compositional `VANILLA_VILLAGE × THREAT_ONLY` pair; components occur elsewhere.

The generator covers all 18 benchmark families, six legal environment
classifications, five provider modes, four narrative states, six candidate
kinds, 30 environment/provider combinations, and 22,914 structurally relevant
NO_ACTION evaluations. State and candidate input fingerprints have zero
cross-split duplicates and there are zero target conflicts.

`teacherQuality` comes from `generate_experience_v3.py::quality` and
`teacherChosen` is its deterministic eligible-set argmax. This is
`DETERMINISTIC_BENCHMARK_TEACHER` supervision, not real gameplay outcome data.
Targets, IDs, seeds, split labels and post-decision fields are excluded from
inputs. Hard safety is not learned.

The 1,000-state pilot replay was byte-identical. The 10,000-state pilot passed
the same structural audit. The final corpus was copied shard-by-shard to
Kaggle and manifest/shard hashes matched. A V3 GPU smoke loaded TRAIN-only
preprocessing, ran forward/backward and three optimizer steps on a 256-row
batch, validated a 256-row batch and wrote a temporary checkpoint successfully.

The final `TRAIN/part-00000.jsonl` was independently regenerated with the same
seed/spec and matched byte-for-byte. Final manifest SHA-256 is
`10de28923e0251d67befd53b25400c781f7e6315d96250d5db616e69eb788b7d`; the
shard hash file SHA-256 is
`d18044d087e49e02c1d64f7b0db51e1c3eedaef59df675af9769b51aff68273b`.

No V3 full training was performed and Shadow Mode remains disabled. The
persistent dataset is outside Git; only generator, manifests, reports and gate
tooling are versioned.
