# DIRECTOR Experience V3 coverage

Coverage is structural and does not tune on TEST/HOLDOUT targets.

## Categorical observations

**family**
BUILDING_TENSION=17896, CONTINUATION_OPPORTUNITY=17897, DIMENSION_ISOLATION=17891, FALSE_HIGH_INTENSITY_TEMPTATION=17277, LOW_IMPACT_CONTINUATION=17279, MULTIPLE_VALID_CHOICES=16670, MULTI_PROVIDER=17890, NOVELTY_PRESSURE=17897, NO_ACTION_REQUIRED=17895, PLAYER_BASE_NEARBY=17891, PROVIDER_FIT_CONTRAST=17890, PROVIDER_MISSING=17278, PROVIDER_RETURNED=16665, RECOVERY_REQUIRED=16670, REPETITION_TRAP=16665, THREAD_COMPETITION=17890, THREAD_DORMANCY_RESUME=17891, WILDERNESS=17283

**environment**
ESTABLISHED_PLAYER_AREA=50000, PLAYER_MODIFIED_AREA=50000, TEMPORARY_CAMP=53684, UNKNOWN=53670, VANILLA_VILLAGE=53676, WILDERNESS=53685

**providerMode**
ACTOR_ONLY=60000, ALL_AVAILABLE=67362, NONE=60000, STRUCTURE_ONLY=60000, THREAT_ONLY=67353

**narrativeState**
ACTIVE=78681, DORMANT=78686, NONE=78681, SUSPENDED=78667

**candidateKind**
ambient=60000, continuation=60000, exploration=60000, no-action=60000, recovery=60000, threat=14715

**provider**
none=120000, structure=180000, threat=14715

## Numeric ranges
tension: min=1.526e-05 max=0.999969 mean=0.499977 std=0.288672
pressure: min=5.65e-06 max=0.999983 mean=0.499984 std=0.290544
fatigue: min=1.28e-05 max=0.999962 mean=0.504641 std=0.290251
recoveryNeed: min=8.5e-06 max=0.999958 mean=0.499947 std=0.288684
healthRatio: min=0.450003 max=0.999973 mean=0.724972 std=0.158779
combatPower: min=2.69e-06 max=0.999968 mean=0.499954 std=0.288673
isolation: min=1.197e-05 max=0.999928 mean=0.499948 std=0.28868
eventConcurrency: min=0 max=4 mean=1.99974 std=1.41416
memoryPressure: min=0 max=4 mean=1.99977 std=1.41434
recentHigh: min=0 max=2 mean=1.1906 std=0.747689
repetitionCount: min=0 max=2 mean=0.904614 std=0.825589
threadAgeBucket: min=0 max=3 mean=1.21411 std=0.988779
episodeStep: min=0 max=9 mean=4.54694 std=2.87204

## Combination and temporal coverage
environment/provider combinations observed: 30
episodes: 1000; episodic states: 10000; all episode steps are assigned as one group.
NO_ACTION relevant candidate evaluations: 22914
boundary states (finite numeric feature near edge): 232789
state groups by split: {'TRAIN': 35009, 'VALIDATION': 11505, 'TEST': 5641, 'HOLDOUT': 7845}
