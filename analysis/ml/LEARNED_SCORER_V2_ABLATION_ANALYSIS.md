# V2 diagnostic ablations

The frozen scorer was evaluated with input masks only; weights were not
updated. Results are recorded in the external closure diagnostics artifact.
`numeric-only`, `categorical-only`, memory/repetition removal,
provider/context removal, and temporal-context removal are diagnostic probes
on VALIDATION, not alternate models and not tuning decisions.
