# Scoring

All component scores are normalized to `0..100`. Hard correctness is computed from objective legality: no-action requirements, provider availability, safety context and valid plan state. Decision quality averages independent scenario attributes: context relevance, continuity, novelty, provider fit, impact appropriateness and intensity appropriateness.

Temporal quality measures repetition rate over sequence output. Efficiency measures invalid or avoidable choices separately. V1 composite weights are fixed: hard correctness `40%`, decision quality `30%`, temporal quality `20%`, efficiency `10%`. Any hard safety violation invalidates a run regardless of composite score.

Future comparison uses `candidate / baseline`, absolute difference and percentage difference. The required name is `DECISION_QUALITY_RELATIVE_IMPROVEMENT`; no intelligence multiplier is reported. Zero baseline returns null safely.
