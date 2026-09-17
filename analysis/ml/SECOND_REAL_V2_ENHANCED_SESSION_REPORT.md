# Second real V2 enhanced-telemetry session audit

## Result

`CLASSIFICATION=PASS_SECOND_REAL_V2_ENHANCED_SESSION_WITH_TELEMETRY_FIX`

The real collection session was `cs_M2QY5UMKMCFQ`. It contains 166 valid V2
events, 40 complete decision triplets, 40 factual NO_ACTION terminals, and
one interrupted/reconnected participant experience represented by two
consent-scoped participant sessions.

The two participant sessions are proven by operational login evidence:
one connection ended with reset at 18:47:30 and a new connection began at
18:47:34. Both had persisted ACCEPTED consent and DISCONNECT ends. They are
not counted as two independent field sessions; this is one field session with
a reconnect.

## Decisions and diagnostics

All 40 decisions were:

- `candidateCount=1`
- `actionCandidateCount=0`
- `eligibleActionCandidateCount=0`
- `noActionCandidateCount=1`
- `selectionReasonCode=NO_ACTION_ONLY_CANDIDATE`

Thus the previous minimal-stack diagnosis is supported directly by runtime
telemetry: no action candidate was generated. No safety rejection or
feasibility rejection occurred. Provider/goal/blueprint details were not
invoked by this decision path and remain explicitly `NOT_CAPTURED`.

Shadow also selected NO_ACTION 40/40. This is descriptive agreement, not
accuracy or a model-quality label. No action execution or execution outcome
occurred.

## Health consistency

The discrepancy was a stale-snapshot/status semantic issue, not evidence of
missing scores:

- RAW events: 166 lines;
- RAW score results: 40;
- initial health snapshot: sequence 2, before gameplay, so requests/success
  were 0 and writerEvents was 1;
- status script reported that initial snapshot rather than total persisted
  events.

The V2 service now emits health on participant end and the status script also
shows persisted event/score counts. Queue high-watermark measurement now
records at least one slot after a successful enqueue, avoiding a race where
the worker consumed the item before measurement. The audited RAW is retained
unchanged; no health values were backfilled into it.

## Integrity and storage

The historical V1 file remains unchanged. V2 RAW SHA:
`e5eb33ee12dc8e897dc7021d9173375ecc7f1552f8d6194137a5b47a4dc3f4a3`.
It was imported hash-preservingly and canonicalized into 40 descriptive
records, all with `trainingAllowed=false`. Privacy scan passed.

The observability-only rebuild was deployed as Java 8 class-major 52 JAR
`dbbe097a3d8dca144467d27fd9cfc6bf2a28a303e981cb06796c0fb0067254d0`.
The frozen V4 model SHA remains
`dd4ccebad2b7f94db74a27493c2418e7abdea5ee66509d39e2a3982538592af9`.

Field progress is two valid real field sessions total. One further real
session is required for the three-session field minimum; the newly deployed
boot session is empty and is not counted.
