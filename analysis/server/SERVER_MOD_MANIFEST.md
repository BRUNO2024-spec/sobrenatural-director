# Research server mod manifest

## Selected dedicated stack

| MOD | VERSION | JAR | SHA-256 | SERVER PATH | CLIENT PATH | SERVER | CLIENT | DEPENDENCY | STATUS |
|---|---|---|---|---|---|---|---|---|---|
| Forge | 10.13.4.1614 | `forge-1.7.10-10.13.4.1614-1.7.10-universal.jar` | `00d1ca02192c7efb87da95552ebf247021f1e1d642f86d0a03076314def11529` | server root | launcher-managed | yes | Forge 1.7.10 required | Minecraft 1.7.10 | boot PASS |
| Sobrenatural Director | 0.12.0-alpha | `SobrenaturalDirector-0.12.0-alpha.jar` | `e1799f67b09a5bfbf45df3203dc74d473adc85e1010d37be90d8138e0cb459bf` | `mods/` | none | yes | no | Forge | dedicated boot PASS; remote client optional |

The V4 JSON is an external artifact under `models/`, not a Forge mod. The
Director declares `acceptableRemoteVersions="*"`; clients may omit the
server-only Director while integrated-server support remains preserved.

FNaF Universe, ObsidianAPI, and all unproven original mods are not installed.
