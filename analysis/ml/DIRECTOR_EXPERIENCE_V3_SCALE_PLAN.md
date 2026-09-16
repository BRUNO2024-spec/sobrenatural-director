# V3 scale plan

The generator writes canonical JSONL in 10,000-row shards and holds only one
state/candidate set in memory. The 60k run used 266 MB on disk, 69 MB peak RSS,
and 11.7 seconds wall time on this VPS.

Approximate extrapolation from the measured 5.245 candidates/state:

| states | candidate rows | disk | peak working memory | implication |
|---:|---:|---:|---:|---|
| 100k | 524k | ~440 MB | O(1 state) | suitable streaming generation |
| 250k | 1.31M | ~1.1 GB | O(1 state) | shard/transfer management dominates |
| 500k | 2.62M | ~2.2 GB | O(1 state) | use chunked sync and streaming loaders |
| 1M | 5.25M | ~4.4 GB | O(1 state) | future phase; training throughput/storage planning required |

These are engineering estimates, not generated datasets. No full V3 training
was performed.
