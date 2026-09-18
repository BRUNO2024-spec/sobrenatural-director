# V2 situation/thread telemetry matrix

| Event family | Required facts | Training semantics | Privacy |
|---|---|---|---|
| `SITUATION_*` | opaque situation ID, goal, blueprint, action plan, lifecycle, tick | `trainingAllowed=false` | no player identity |
| `THREAD_*` | opaque thread/situation IDs, thread state, reason, tick | `trainingAllowed=false` | no player identity |
| terminal linkage | factual lifecycle and action-plan reference | no reward/counterfactual | provider stack only by fingerprint |
