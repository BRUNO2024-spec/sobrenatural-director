# Future real-world experience collection plan

A future shadow-only collector should record pre-decision state, complete legal
candidate set, heuristic decision, learned score, safety rejections,
replans/aborts, and later observable outcomes such as continuation,
resolution time and player reaction proxies. The learned scorer must initially
remain non-authoritative; deterministic safety, authorization, rollback and
kill-switch boundaries remain external.

Persist only minimized, pseudonymous aggregates. Avoid player identifiers,
coordinates and raw chat unless explicitly required; define retention,
redaction and consent policies. Join delayed outcomes by an opaque episode
record, never by future data fed back into the original input. Real gameplay
outcomes must be labelled separately from synthetic teacher supervision.
