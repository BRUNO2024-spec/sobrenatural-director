# Passo 4 — Semantic Action Catalog and Composition

## Classification

`PARTIAL_SEMANTIC_ACTION_CATALOG_NARRATIVE_BRIDGE`

The shared semantic catalog, deterministic action model, narrative bridge,
candidate adapter, bounded DAG composition and public plan-only bootstrap are
implemented and validated. The public server remains safe: Autonomous
Execution is off, no participant sessions are active, and no Phase 4 gameplay
mutation occurred.

## Verified

- 11 deterministic definitions: 7 executable primitive definitions and 4
  composite/unavailable definitions.
- Goal, blueprint and role information map into shared semantic intents.
- Legacy decision candidates can be adapted without changing the V4 model or
  historical records.
- Provider/content/dimension availability is diagnosed fail-closed.
- Action plans are immutable, dimension-aware and cycle-checked.
- Forge Java tests: 333 pass; build/reobf: pass; class major: 52.
- Disposable/public boot: pass; providers: CustomNPCs, GraveStone, SlenderMan.

## Explicit remaining boundary

`ActionPlan` currently stops at deterministic plan/preflight metadata. The
semantic plan-to-concrete-provider executor, provider-unregister/content-removal
stale-plan injection, compensation-failure E2E and action-level telemetry were
not enabled or fabricated. Existing low-level provider controls remain the
authoritative mutation boundary. Those items belong to the controlled
execution phase, not public autonomous gameplay.
