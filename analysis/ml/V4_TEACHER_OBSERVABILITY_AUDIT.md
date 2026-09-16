# V4 teacher observability audit

The teacher is `ml/generate_experience_v3.py::quality`. It uses state context,
provider availability, and candidate kind; all are pre-decision V3/V4 inputs.
Teacher score, chosen flag, IDs, seeds, policy decisions, and post-decision
signals are excluded. `TEACHER_SIGNAL_COUNT=16`, `MODEL_OBSERVABLE_COUNT=16`,
and `MISSING_LEGITIMATE_SIGNAL_COUNT=0`.
