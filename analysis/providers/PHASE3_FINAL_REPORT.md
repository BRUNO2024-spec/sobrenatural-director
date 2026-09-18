# Passo 3 — Final report

## Classification

`PASS_CONCRETE_PROVIDER_WORLD_CONTENT_CONTROLS`

The approved stack now has concrete lifecycle/world paths. Autonomous
execution remains off; this is provider API readiness, not permission to run
unbounded gameplay actions.

## Implemented

- **CustomNPCs:** version-pinned, lazy bridge; semantic lifecycle requests map
  to Director-owned create/remove requests; journal and compensation are used.
- **SlenderMan:** threat lifecycle requests map to the existing safe-site,
  ownership-marker and threat-journal path; natural entities are not adopted.
- **GraveStone:** existing allowlisted composition executor remains the world
  mutation boundary, with journal, idempotency, dimension matching, player
  protection and stale-safe rollback.
- **Unified routing:** `ConcreteControlRouter` performs provider availability,
  capability, dimension, authority and lease checks before provider dispatch.
  Server-thread runtime context is separate from async-safe requests.

## Deliberate unsupported controls

CustomNPCs/SlenderMan attributes, movement, behavior, target and equipment
are not advertised: the available API/runtime evidence does not prove a safe
deterministic contract. No item/weapon/equipment or dimension provider met the
approved dedicated-runtime criteria. FNaF/Obsidian remain excluded by the
known client classloading incompatibility. These outcomes are recorded in the
coverage report and are fail-closed, not missing work.

## Safety and dimension closure

The previous dimension-zero assumptions in CustomNPCs, SlenderMan and
composition preflight were replaced by explicit equality with the target
`WorldServer` dimension. Player-owned/unknown content remains protected.
Provider exceptions, expired leases, unavailable providers and stale targets
return typed failure results.

## Validation

| Gate | Result |
|---|---|
| Java tests | 326 PASS |
| Python tests | 61 PASS |
| Core external mod imports | 0 |
| Disposable reobfuscated Forge runtime | PASS |
| Public cold boot | PASS; 3 providers |
| Build/reobf | PASS |
| class major | 52 |
| JAR SHA-256 | `95f4db884ba8fc64ce8a8810310900c3592a894bf719be0ebaefd4120d122c8d` |
| Autonomous Execution / Learned Control / Advisory | OFF |
| New field sessions / real mutations | 0 / 0 |

Machine-readable closure: `runtime-tests/control/concrete-provider-controls-closure.properties`.
