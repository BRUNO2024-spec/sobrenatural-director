# Public Shadow Research Server foundation

CLASSIFICATION=PARTIAL_PUBLIC_SHADOW_RESEARCH_SERVER_FOUNDATION
FINAL_GATE=FAIL

## Confirmed

- Isolated root: `/home/desktop/Documentos/director-shadow-server`
- Forge: `1.7.10-10.13.4.1614`
- Java: `1.8.0_502`
- Local listener: `*:25565`
- Dedicated boot: PASS after `reobf`; clean shutdown: PASS
- Selected mod: Director only; FNaF/Obsidian excluded due known LWJGL dedicated blocker
- Whitelist: ON; online authentication: ON; RCON/query: OFF
- Protected world and `original_mods` were not modified

## Not yet safe to expose to players

The current Director artifact contains the consent command and session-only
opaque participant registry, but V4 model loading is not wired into the Forge
runtime and consent does not yet gate the shadow service/JSONL collector.
Therefore `SHADOW_SERVICE_ENABLED=NO`, `SHADOW_MODEL_LOAD=FAIL_NOT_WIRED`, and
no research gameplay data may be collected yet. This is intentionally a
blocking result rather than a fabricated PASS.

Oracle ingress was not changed or confirmed by this environment. The detected
public address is `144.22.149.197`, but external connectivity remains pending.
