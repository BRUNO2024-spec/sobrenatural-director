# REAL_PROVIDER_STACK client manifest

Status: **READY / client installation required before connection**. The public
server now runs the approved provider stack; no gameplay session was started.

The approved candidate providers add client-visible content and therefore must
be installed on the matching Minecraft 1.7.10 client before connection:

| File | Version | SHA-256 | Client required | Server required | Why |
|---|---|---|---|---|---|
| `CustomNPCs_1.7.10d(19jun17).jar` | 1.7.10d | `73d0036752d3931da5ed6bf5e7948185ee70c6cc46b67a2b726f24ef2b3292eb` | YES | YES | registered actor/entity/content provider |
| `GraveStone-2.13.0.jar` | 2.13.0 | `a3df09c5229aa4678fcc68af0277efbfe70e321a4657f91bce1ea0ef3bcf1874` | YES | YES | registered blocks/items/entities and client proxy/content |
| `SlenderMan-3.3_1.7.10.jar` | 3.3_1.7.10 | `f2a8a4592130b36cfd8cc4c322d7eea23dfbdf2a868c2d7b7db2b1de3427eab7` | YES | YES | registered entity/block content and threat provider |

Source path for all three: `/home/desktop/Documentos/SOBRENATURAL-DIRECTOR/original_mods/`.

For Pojav, place the exact files in the `mods/` directory of the Minecraft
1.7.10 instance used by the client. No Android absolute path is inferred.
