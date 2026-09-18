# Multi-dimension assumption audit

## Result

`PASS` for the new foundation. New references require `DimensionRef`, and the
existing persisted world model already stores dimension on `KnownLocation` and
`ReservedRegion`. No new API uses dimension zero as an implicit default.

## Existing bounded assumptions

| Location | Finding | Disposition |
|---|---|---|
| threat execution bridge | runtime fixture uses dimension `0` as a spawn-site fallback | existing Phase 1 execution path; not reused by foundation and remains gated |
| environment/observation models | dimension is carried by runtime context where available | reused; no duplicate dimension state introduced |
| legacy spatial model | integer dimension fields exist in persisted records | backward-compatible; new `WorldPositionRef` wraps semantic identity |
| V4 feature vector | no dimension feature added | unchanged by design |

The audit does not claim that the whole historical Director is multi-dimension
complete. It proves that Phase 2 contracts cannot create an ambiguous spatial
reference and that cross-dimension mutation requires an explicit target.
