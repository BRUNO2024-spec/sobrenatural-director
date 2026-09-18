# Situation recovery matrix

| Failure window | Recovery policy | Duplicate risk | Result |
|---|---|---:|---|
| Before ActionPlan start | Keep PLANNED/READY; no replay | none | caller may revalidate |
| During execution, journal unknown | Suspend; require provider-journal reconciliation | prevented | no blind replay |
| ActionOutcome persisted, situation transition missing | reconcile situation only | prevented | factual outcome required |
| Situation terminal, memory commit missing | commit memory by opaque situation ID | prevented by memory idempotency | history repaired |
| Situation terminal, thread commit missing | link/transition thread only | prevented by membership idempotency | thread repaired |
| Dimension unavailable after restart | remain suspended/stale | none | no wrong-dimension mutation |
