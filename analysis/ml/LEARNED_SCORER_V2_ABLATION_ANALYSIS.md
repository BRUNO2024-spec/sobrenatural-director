# V2 diagnostic ablations

The frozen scorer was evaluated with input masks only; weights were not
updated. On VALIDATION, baseline pairwise/regret was `0.80162/0.01541`.
Numeric-only was `0.32496/0.30532`, categorical-only `0.65275/0.07346`,
without memory/repetition `0.63140/0.08771`, without provider/context
`0.63640/0.08452`, and without temporal/context `0.63604/0.08496`.
All are diagnostic probes, not alternate models or tuning decisions.
