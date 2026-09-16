# Research server mod manifest

## Selected dedicated stack

| MOD | VERSION | JAR | SHA-256 | SERVER PATH | CLIENT PATH | SERVER | CLIENT | DEPENDENCY | STATUS |
|---|---|---|---|---|---|---|---|---|---|
| Forge | 10.13.4.1614 | `forge-1.7.10-10.13.4.1614-1.7.10-universal.jar` | `00d1ca02192c7efb87da95552ebf247021f1e1d642f86d0a03076314def11529` | server root | launcher-managed | yes | Forge 1.7.10 required | Minecraft 1.7.10 | boot PASS |
| Sobrenatural Director | 0.12.0-alpha | `SobrenaturalDirector-0.12.0-alpha.jar` | `8528b1edf959b9501bbea5cc4878ce581f350299179de973f201e04963ca8451` | `mods/` | none | yes | no | Forge | dedicated boot PASS |

The V4 JSON is an external artifact under `models/`, not a Forge mod. The
current public-server template leaves Shadow disabled because the V4 Java
loader/consent collection wiring is not yet complete; enabling it would be
misleading.

FNaF Universe, ObsidianAPI, and all unproven original mods are not installed.
