# Enhanced real field result audit

## Result

`CLASSIFICATION=PASS_ENHANCED_REAL_FIELD_RESULT_AUDIT`

`cs_HIO32WRPKPXP` is the latest real enhanced collection. It contains 670
valid V2 events, 166 complete decision triplets, one consent-scoped
participant session, and 166 terminal `NO_ACTION` records. External server
evidence shows a real client connection from 07:33:17 to 08:01:05; the
persisted participant session starts and ends with `ACCEPTED` consent and
`DISCONNECT`.

The earlier enhanced collection `cs_M2QY5UMKMCFQ` remains unchanged at 166
events and 40 complete triplets. Its two participant records are one real
experience split by a reconnect: the first ended at server tick 1567262 and
the second began at 1574632. Both had `ACCEPTED` consent and `DISCONNECT`.
The first external connection ended with `Connection reset by peer` and a new
login followed four seconds later.

## Decision diagnostics

All 206 enhanced decisions across M2 and HIO have the same factual shape:

- `candidateCount=1`;
- `actionCandidateCount=0`;
- `eligibleActionCandidateCount=0`;
- `noActionCandidateCount=1`;
- `rejectedCandidateCount=0`;
- safety and feasibility rejections `0`;
- `selectionReasonCode=NO_ACTION_ONLY_CANDIDATE`.

No action candidate or eligible action candidate was generated. The captured
provider counters are `providerCandidateCount=0`, `availableProviderCount=0`,
and `missingProviderCount=0`; registered/eligible provider counts,
capability lists, planning state, goal trace, and blueprint trace are not
captured. Therefore the minimal provider-stack explanation is
`STRONGLY_SUPPORTED`, not proven from a missing-capability field.

Actual and Shadow both selected `candidate:no_action` in all 206 decisions.
This is descriptive agreement, not accuracy. There were no execution starts,
execution terminals, or execution outcomes; their absence is expected because
all decisions were NO_ACTION.

## Health reconciliation

The current HIO collection is internally reconciled:

- 166 score requests;
- 166 successful scores;
- 0 scorer errors;
- queue drops 0;
- queue high-watermark 1;
- persisted events 670;
- latest health snapshot at event 670 reports writerEvents 669 because the
  snapshot serializes the writer sequence before emitting itself.

`WRITER_EVENTS` is therefore a pre-snapshot event-sequence value, not a
persisted-line total. The status script separately reports
`PERSISTED_EVENT_COUNT=670`. M2's only health snapshot was the boot snapshot,
so its zero scorer values and `writerEvents=1` are historical snapshot timing,
not missing scores. No counter bug or runtime bypass was found.

The scorer request/success callsites are the worker loop immediately before
and after `scorer.score`; the same metrics object is bound into the writer and
read by health/status. Queue instrumentation is on the actual bounded queue;
HIO's watermark of 1 confirms use of that queue.

## Integrity

HIO RAW SHA:
`5b074d70ed50950ffdd795c1b233c8075ec6dd898040ad37d2ec8fd6d1a22938`.
Its imported copy has the identical SHA. Validation returned zero parse
errors, duplicate events, or orphan decision records; event sequences are
1..670 and all records use V2. Privacy scan passed and
`trainingAllowed=true` occurred zero times. Canonicalization produced 166
records, all descriptive NO_ACTION records.

The three valid real V2 experiences total 338 scored decisions and 338
terminal decisions. There are zero action decisions and zero complete
execution outcomes. Behavioral diversity is `NO_ACTION_SATURATED` with
`INSUFFICIENT_ACTION_COVERAGE`; pipeline integrity is validated.

## Strategic decision

`THIRD_BASELINE_SESSION_VALUE=LOW`. The current real corpus already has 132,
40, and 166 decisions, while both enhanced sessions repeat the same candidate
and reason-code distribution with no action candidates. Do not run another
baseline on the same stack. The next phase should be planned, not executed in
this audit: a dedicated-compatible provider stack and real action-experience
foundation, without changing V4, heuristics, safety, or control modes.
