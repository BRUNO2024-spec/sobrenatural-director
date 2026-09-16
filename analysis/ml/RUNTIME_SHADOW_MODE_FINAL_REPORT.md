# Runtime Shadow Mode foundation — final report

CLASSIFICATION=PASS_RUNTIME_SHADOW_MODE_REAL_EXPERIENCE_COLLECTION_FOUNDATION
FINAL_GATE=PASS

The implementation is observation-only and default-off. The exact pre-decision
boundary is `DecisionEngine.decide(...)`; the execution boundary remains
`DirectorRuntimeCoordinator` and its existing controlled/safety path. The
shadow hook copies eligible candidates and context into immutable DTOs, submits
them with `offer`, and never returns a plan or execution request.

`ShadowObservationService` uses a bounded queue and daemon worker;
`JsonlShadowEventWriter` is local-only, size-rotated, and budget-limited.
`ShadowOutcomeTracker` has bounded TTL; incomplete outcomes are not fabricated.
The staging builder forces `trainingAllowed=false` and rejects malformed input.

Validation: Java 8 `./gradlew test jar --no-daemon --max-workers=1` passed,
300 tests. JAR SHA-256 is
`00e89725e8ed60abf1340ccd529c18dd25be9b1bbba196c27bd90f6be2a4b48b` and the
artifact class major is 52. Python tooling compiles and the acceptance
verifier passes.

The frozen V4 checkpoint remains external and unchanged; the Java export is
validated by frozen provenance metadata and is not committed to Git. No real
client gameplay session was available on this ARM64 environment:
`FIELD_REAL_GAMEPLAY_SESSION_COUNT=0`,
`REAL_GAMEPLAY_EXPERIENCE_COLLECTED=NO`, and
`LEARNED_CONTROL_READINESS=INSUFFICIENT_EVIDENCE` remain authoritative.
