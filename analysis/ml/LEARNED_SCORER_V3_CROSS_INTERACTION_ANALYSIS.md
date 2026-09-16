# V3 cross interaction analysis

The predefined X01–X10 map was implemented diagnostically. Validation results
did not show a stable gain: MLP (3,625 parameters) was preferable to explicit
crosses (5,161) and Cross Network depth 1/2 (3,727/3,829) across seeds 7/17/27.
The no-cross MLP was therefore frozen; no learned interaction is claimed.

The diagnostic evidence still supports contextual representation: removing
memory/repetition, provider/context, or temporal/context features degraded
validation ranking. Sequence states remain harder than static states. The
worst blind slice analysis is recorded separately; none of these observations
changed the frozen model after blind evaluation.
