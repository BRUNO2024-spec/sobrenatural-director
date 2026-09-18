# REAL_PROVIDER_STACK server manifest

Status: **DEPLOYED AND BOOT-VALIDATED**. The public server runs this stack with
autonomous execution disabled.

## Candidate approved stack

| File | Version | SHA-256 | Server required | Deployment decision |
|---|---|---|---|---|
| `SobrenaturalDirector-0.12.0-alpha.jar` | current reobfuscated artifact | `4a63c2bef252c0bc11c9d75ece950ae31b1585152ba1ae54145069b6803dc911` | YES | deployed and boot-validated |
| `CustomNPCs_1.7.10d(19jun17).jar` | 1.7.10d | `73d0036752d3931da5ed6bf5e7948185ee70c6cc46b67a2b726f24ef2b3292eb` | YES | deployed and registry-validated |
| `GraveStone-2.13.0.jar` | 2.13.0 | `a3df09c5229aa4678fcc68af0277efbfe70e321a4657f91bce1ea0ef3bcf1874` | YES | deployed and registry-validated; backup warning documented |
| `SlenderMan-3.3_1.7.10.jar` | 3.3_1.7.10 | `f2a8a4592130b36cfd8cc4c322d7eea23dfbdf2a868c2d7b7db2b1de3427eab7` | YES | deployed and registry-validated |

## Excluded

FNaF/Obsidian stack remains excluded due to the documented dedicated
`net/minecraft/client/renderer/entity/Render` classloading failure. No
excluded binary is listed for deployment.
