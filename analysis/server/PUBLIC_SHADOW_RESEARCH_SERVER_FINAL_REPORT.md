# Public Shadow Research Server foundation

CLASSIFICATION=PASS_PUBLIC_SHADOW_RUNTIME_COLLECTION_CLOSURE
FINAL_GATE=PASS

## Confirmed

- Isolated root: `/home/desktop/Documentos/director-shadow-server`
- Forge: `1.7.10-10.13.4.1614`
- Java: `1.8.0_502`
- Local listener: `*:25565`
- Dedicated boot: PASS after `reobf`; clean shutdown: PASS
- Selected mod: Director only; FNaF/Obsidian excluded due known LWJGL dedicated blocker
- V4 loader: READY on dedicated boot; strict SHA/config/schema/weight checks
- Collection gate: service enabled only when every active player has accepted
  session consent; declined/revoked sessions enqueue nothing
- Synthetic closure harness: 1000 V4 inference → JSONL → outcome records
- Whitelist: ON; online authentication: ON; RCON/query: OFF
- Protected world and `original_mods` were not modified

## Field status

The runtime closure is ready for controlled field validation. It has not been
used with real players and no gameplay research data exists yet.

Oracle ingress was not changed or confirmed by this environment. The detected
public address is `144.22.149.197`, but external connectivity remains pending.
