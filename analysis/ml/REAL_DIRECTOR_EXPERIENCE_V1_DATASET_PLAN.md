# REAL_DIRECTOR_EXPERIENCE_V1 dataset plan

Raw sessions → schema/integrity validation → canonical joins → outcome
completeness → filtering and analysis → separately approved label/reward
design → session/episode-level TRAIN/VALIDATION split. Random row splits are
not permitted because one gameplay episode must remain in one partition.
