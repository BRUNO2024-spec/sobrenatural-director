# Dedicated-server mod compatibility audit

Forge 1.7.10 dedicated compatibility is not inferred from client/integrated success. The research server selects only the Director after a clean dedicated boot. No JAR under `original_mods/` was modified.

| MOD/JAR | SHA-256 | PURPOSE | RESEARCH SERVER | RESULT | SOURCE |
|---|---|---|---|---|---|
''+'
'.join(rows)+'

## Explicit findings

- `FNaFUniverseMod-1.7.10~v10.4~.jar` plus `ObsidianAPI-0.1.1.jar` is excluded: previous dedicated boot failed while loading `org.lwjgl.util.vector.Quaternion`. This is the known upstream limitation; no bytecode/JAR repair was attempted.
- `addons/FNaFUniverseSpawnAddon-0.9.1-alpha.jar` is also excluded because its target provider is absent and its dedicated compatibility is not independently proven for this exact public stack.
- `SlenderMan`, `CustomNPCs`, and `GraveStone` have prior isolated dedicated evidence, but are not selected until a new public-server compatibility test and client distribution decision.
- Forge libraries are runtime dependencies, not gameplay mods.
