# Server-only handshake bug analysis

## Root cause

`SobrenaturalDirector` had no `acceptableRemoteVersions` value in its Forge
`@Mod` annotation. Forge 1.7.10 consequently treated the server mod as a
required remote mod and rejected a client whose mod list omitted
`sobrenaturaldirector`.

## Current policy

The annotation now declares `acceptableRemoteVersions="*"`. This is the
official Forge remote compatibility mechanism and permits a Forge-compatible
client without the Director mod. `serverSideOnly` was not set, preserving
physical-client loading compatibility for integrated-server use.

## Client dependency audit

The Director source has no `SimpleNetworkWrapper`, `FMLEventChannel`, custom
packet registration, client proxy, renderer, GUI, block, item, biome,
dimension, or synchronized entity registration. The `DirectorEntityMetadata`
and `ExternalBlockPolicy` names are pure server-side data/policy classes; they
are not Forge content registrations. The mod therefore has no client content
dependency for the dedicated research server.

`CLIENT_REQUIRED_BY_FML_WHY=old annotation default required remote mod`.
`CLIENT_NEEDS_DIRECTOR=NO` after this correction.
