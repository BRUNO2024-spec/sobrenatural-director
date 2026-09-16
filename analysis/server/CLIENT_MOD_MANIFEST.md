# Client mod manifest

For the currently validated Director-only server, install **Minecraft 1.7.10
with Forge 10.13.4.1614** in the client launcher. No gameplay mod JAR is
required on the client for `SobrenaturalDirector-0.12.0-alpha.jar`.

| CLASS | FILE | CLIENT DESTINATION | SOURCE | STATUS |
|---|---|---|---|---|
| BOTH protocol/runtime | Forge 1.7.10-10.13.4.1614 | launcher instance selected by the user | `NEEDS_USER_PROVIDED_SOURCE` | required |
| SERVER ONLY | `SobrenaturalDirector-0.12.0-alpha.jar` | none | server-local build | do not copy |
| CLIENT ONLY | none | none | none | no client-only mod selected |

FNaF/Obsidian and other client packs are intentionally not part of this
server. Adding them would require a separately audited matching server pack.
