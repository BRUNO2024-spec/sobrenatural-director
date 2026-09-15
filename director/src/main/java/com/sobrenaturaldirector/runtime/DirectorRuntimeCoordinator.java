package com.sobrenaturaldirector.runtime;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sobrenaturaldirector.content.catalog.SemanticCatalog;
import com.sobrenaturaldirector.content.catalog.SemanticCatalogResource;
import com.sobrenaturaldirector.config.FoundationConfig;
import com.sobrenaturaldirector.content.minecraft.ForgeContentAvailabilityProbe;
import com.sobrenaturaldirector.content.model.ContentKind;
import com.sobrenaturaldirector.content.model.ContentKey;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.content.model.RuntimeContentIndex;
import com.sobrenaturaldirector.content.query.PlanRequirementResolution;
import com.sobrenaturaldirector.content.query.ResolutionRequest;
import com.sobrenaturaldirector.content.query.ResolutionResult;
import com.sobrenaturaldirector.content.resolver.SemanticContentResolver;
import com.sobrenaturaldirector.composition.CompositionEngine;
import com.sobrenaturaldirector.composition.ControlledCompositionExecutor;
import com.sobrenaturaldirector.provider.ProviderBootstrap;
import com.sobrenaturaldirector.composition.model.CompositionRequest;
import com.sobrenaturaldirector.composition.model.CompositionResult;
import com.sobrenaturaldirector.composition.model.StructureScale;
import com.sobrenaturaldirector.derivation.DecisionContextAssembler;
import com.sobrenaturaldirector.derivation.DerivedModels;
import com.sobrenaturaldirector.derivation.ObservationDerivationPipeline;
import com.sobrenaturaldirector.domain.DirectorWorldState;
import com.sobrenaturaldirector.decision.DecisionEngine;
import com.sobrenaturaldirector.decision.DecisionResult;
import com.sobrenaturaldirector.decision.model.Intent;
import com.sobrenaturaldirector.model.history.HistorySummaryBuilder;
import com.sobrenaturaldirector.observation.model.ObservationFrame;
import com.sobrenaturaldirector.observation.runtime.ObservationCoordinator;
import com.sobrenaturaldirector.environment.runtime.EnvironmentObservationService;
import com.sobrenaturaldirector.environment.model.RegionKey;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.planner.DirectorPlanner;
import com.sobrenaturaldirector.planner.model.DirectorPlan;
import com.sobrenaturaldirector.planner.model.PlanningContext;
import com.sobrenaturaldirector.planner.model.PlanningResult;
import com.sobrenaturaldirector.spatial.AbstractSpatialPlanner;
import com.sobrenaturaldirector.spatial.model.SpatialBudget;
import com.sobrenaturaldirector.spatial.model.SpatialPlanningResult;
import com.sobrenaturaldirector.mutation.MutationTransactionPlanner;
import com.sobrenaturaldirector.mutation.model.MutationBudget;
import com.sobrenaturaldirector.mutation.model.MutationTransactionPlan;
import com.sobrenaturaldirector.narrative.NarrativePlanLifecycleCoordinator;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.execution.SafeAutonomousExecutionCoordinator;
import com.sobrenaturaldirector.execution.ExecutionAuthorization;
import com.sobrenaturaldirector.mutation.runtime.MutationExecutionRequest;
import com.sobrenaturaldirector.mutation.runtime.MutationExecutionResult;
import com.sobrenaturaldirector.mutation.runtime.MutationOperation;
import com.sobrenaturaldirector.execution.ExecutionOutcomeFeedbackCoordinator;
import com.sobrenaturaldirector.execution.ExecutionOutcome;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.block.Block;
import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

/** Server-only adapter that wires existing pure layers and ends at dry-run. */
public final class DirectorRuntimeCoordinator {
    private final ObservationCoordinator observation;
    private final EnvironmentObservationService environment = new EnvironmentObservationService();
    private final FoundationConfig configuration;
    private final ObservationDerivationPipeline derivation = new ObservationDerivationPipeline();
    private final DecisionContextAssembler contexts = new DecisionContextAssembler();
    private final DecisionEngine decisions = new DecisionEngine();
    private final DirectorPlanner planner = new DirectorPlanner();
    private final SemanticContentResolver resolver = new SemanticContentResolver();
    private final CompositionEngine composition = new CompositionEngine();
    private final ControlledCompositionExecutor controlledComposition = new ControlledCompositionExecutor(DirectorRuntimeMode.CONTROLLED_EXECUTION, ProviderBootstrap.blockPolicy());
    private final AbstractSpatialPlanner spatial = new AbstractSpatialPlanner();
    private final MutationTransactionPlanner mutations = new MutationTransactionPlanner();
    private final DryRunMutationSink sink = new DryRunMutationSink();
    private final DirectorRuntimeMetrics metrics = new DirectorRuntimeMetrics();
    private final DirectorProviderRegistry providerRegistry;
    private final NarrativePlanLifecycleCoordinator lifecycle = new NarrativePlanLifecycleCoordinator();
    private final com.sobrenaturaldirector.narrative.NarrativeThreadManager threads = new com.sobrenaturaldirector.narrative.NarrativeThreadManager();
    private final com.sobrenaturaldirector.narrative.AdaptivePacingDirector pacing = new com.sobrenaturaldirector.narrative.AdaptivePacingDirector();
    private final com.sobrenaturaldirector.narrative.ControlledAutonomousPlanningScheduler autonomousScheduler;
    private final com.sobrenaturaldirector.narrative.NarrativeOpportunityGenerator opportunityGenerator = new com.sobrenaturaldirector.narrative.NarrativeOpportunityGenerator();
    private final SafeAutonomousExecutionCoordinator autonomousExecution = new SafeAutonomousExecutionCoordinator();
    private final ExecutionOutcomeFeedbackCoordinator outcomeFeedback = new ExecutionOutcomeFeedbackCoordinator();
    private final SemanticCatalog catalog;
    private DirectorDryRunResult latest;
    private long lastEvaluationTick = Long.MIN_VALUE;
    private long nextEvaluationTick;
    private int stateFingerprint;
    private int lastLifecycleProcessed;
    private final Map<java.util.UUID, com.sobrenaturaldirector.narrative.PacingAssessment> latestPacing = new HashMap<java.util.UUID, com.sobrenaturaldirector.narrative.PacingAssessment>();
    private AutonomousExecutionTestFixture testExecutionFixture;
    private com.sobrenaturaldirector.narrative.PersistentNarrativePlan testExecutionPlan;
    private String lastNormalTickPlanId="", lastNormalTickExecutionId="", lastNormalTickOutcomeId="";
    private int normalTickExecutionAttempts, normalTickApplied, normalTickDuplicateEffects, normalTickFeedbackApplied;

    public DirectorRuntimeCoordinator(ObservationCoordinator observation, FoundationConfig configuration) {
        this(observation, configuration, new DirectorProviderRegistry());
    }

    public DirectorRuntimeCoordinator(ObservationCoordinator observation, FoundationConfig configuration, DirectorProviderRegistry providerRegistry) {
        if (observation == null) throw new IllegalArgumentException("observation is required");
        if (configuration == null) throw new IllegalArgumentException("configuration is required");
        if (providerRegistry == null) throw new IllegalArgumentException("providerRegistry is required");
        this.observation = observation;
        this.configuration = configuration;
        this.providerRegistry = providerRegistry;
        this.autonomousScheduler = new com.sobrenaturaldirector.narrative.ControlledAutonomousPlanningScheduler(configuration.isAutonomousPlanningEnabled());
        this.catalog = SemanticCatalogResource.load(getClass().getClassLoader());
    }

    public void onServerTick(MinecraftServer server, long tick) {
        if (server == null || lastEvaluationTick == tick || tick < nextEvaluationTick) return;
        ObservationFrame frame = observation.getStore().getLatest();
        if (frame == null || frame.getServerTick() != tick) return;
        WorldServer world = overworld(server);
        if (world == null) return;
        lastEvaluationTick = tick;
        nextEvaluationTick = tick + configuration.getObservationIntervalTicks();
        metrics.evaluation();
        try {
            environment.observe(server, tick);
            DirectorWorldSavedData saved = state(world);
            lastLifecycleProcessed = lifecycle.evaluateBounded(saved, providerRegistry, new NarrativePlanLifecycleCoordinator.ProfileLookup() {
                public com.sobrenaturaldirector.environment.model.SemanticRegionProfile get(int dimension, int regionX, int regionZ) {
                    return environment.get(new RegionKey(dimension, regionX, regionZ));
                }
            }, tick);
            threads.evaluateBounded(saved, new com.sobrenaturaldirector.narrative.NarrativeThreadManager.ThreadAssessment() {
                public void assess(com.sobrenaturaldirector.narrative.PersistentNarrativeThread thread, long assessmentTick) { }
            }, tick);
            DirectorWorldState current = saved.getState();
            stateFingerprint = current.hashCode();
            DerivedModels derived = derivation.derive(frame);
            Map<java.util.UUID, com.sobrenaturaldirector.decision.model.DecisionContext> perPlayer =
                    new HashMap<java.util.UUID, com.sobrenaturaldirector.decision.model.DecisionContext>(contexts.assemblePerPlayer(derived, new HistorySummaryBuilder().empty()));
            for (com.sobrenaturaldirector.observation.model.PlayerSnapshot player : frame.getPlayers()) {
                com.sobrenaturaldirector.decision.model.DecisionContext context = perPlayer.get(player.getPlayerId());
                if (context == null) continue;
                RegionKey key = RegionKey.fromChunk(player.getDimension(), (int) Math.floor(player.getX()) >> 4,
                        (int) Math.floor(player.getZ()) >> 4);
                com.sobrenaturaldirector.environment.model.SemanticRegionProfile profile = environment.get(key);
                if (profile != null) perPlayer.put(player.getPlayerId(), context.withEnvironmentProfile(profile));
                com.sobrenaturaldirector.decision.model.DecisionContext pacedContext=perPlayer.get(player.getPlayerId());
                if (pacedContext != null) latestPacing.put(player.getPlayerId(), pacing.assess(player.getPlayerId().toString()+":"+player.getDimension()+":"+key.getX()+":"+key.getZ(), pacedContext, saved.getPacingHistory(), Intent.NO_ACTION));
            }
            if (perPlayer.isEmpty()) {
                executeArmedPersistentPlan(tick, world, saved);
                metrics.noAction();
                latest = result(tick, world.provider.dimensionId, frame, current, Intent.NO_ACTION,
                        null, 0, "NO_PLAYER", "NO_PLAYER", 0, "WAITING_FOR_PLAYER");
                return;
            }
            for (com.sobrenaturaldirector.observation.model.PlayerSnapshot player : frame.getPlayers()) {
                com.sobrenaturaldirector.decision.model.DecisionContext context = perPlayer.get(player.getPlayerId());
                if (context == null) continue;
                evaluate(tick, world, frame, current, context);
                evaluateAutonomousPlanning(tick, world, saved, player, context);
            }
        } catch (RuntimeException failure) {
            metrics.exception();
            latest = result(tick, world.provider.dimensionId, frame, worldStateFingerprint(world),
                    Intent.NO_ACTION, null, 0, "ABORTED", "ABORTED", 0, "EVALUATION_EXCEPTION");
        }
    }

    /** Test-only no-player fixture: it still traverses the production scheduler and plan ledger. */
    private void executeArmedPersistentPlan(long tick, WorldServer world, DirectorWorldSavedData saved) {
        if (testExecutionPlan!=null && saved.getNarrativePlan(testExecutionPlan.getId())==null) saved.recordNarrativePlan(testExecutionPlan);
        if (testExecutionFixture==null || !testExecutionFixture.isAuthorizedByEnvironment() || !configuration.isAutonomousPlanningEnabled()) return;
        for (com.sobrenaturaldirector.narrative.PersistentNarrativePlan plan : saved.getNarrativePlans()) {
            if (plan.getDimension()!=world.provider.dimensionId) continue;
            log(Level.INFO, "armed plan candidate tick=%d id=%s state=%s", tick, plan.getId(), plan.getState());
            String scope="test:"+plan.getDimension()+":"+plan.getRegionX()+":"+plan.getRegionZ();
            com.sobrenaturaldirector.decision.model.DecisionContext context=com.sobrenaturaldirector.decision.model.DecisionContext.calm(tick);
            com.sobrenaturaldirector.situation.SituationContext situation=new com.sobrenaturaldirector.situation.SituationContext(plan.getDimension(),tick,plan.getRegionX(),plan.getRegionZ(),saved.getState().getDirectorSeed(),false,true,true,true,Collections.<String>emptySet(),null);
            long fixtureSeed=saved.getState().getDirectorSeed();
            if (fixtureSeed < 0) fixtureSeed = fixtureSeed == Long.MIN_VALUE ? Long.MAX_VALUE : -fixtureSeed;
            com.sobrenaturaldirector.narrative.NarrativeArbitrationContext arbitration=new com.sobrenaturaldirector.narrative.NarrativeArbitrationContext(context,new com.sobrenaturaldirector.situation.SituationMemory(),Collections.<String>emptySet(),null,null,false,"",fixtureSeed,null);
            com.sobrenaturaldirector.narrative.ControlledAutonomousPlanningScheduler.PlanningCycleResult cycle=null;
            try { cycle=autonomousScheduler.evaluate(new com.sobrenaturaldirector.narrative.ControlledAutonomousPlanningScheduler.CycleInput(scope,tick,scope+"|"+plan.getId(),Collections.singletonList(new com.sobrenaturaldirector.narrative.NarrativeOpportunity("test-opportunity",Intent.valueOf(plan.getIntent()),1.0,1.0,Collections.<String>emptySet(),Collections.<String>emptyList(),false,null)),arbitration,situation,new com.sobrenaturaldirector.situation.SituationMemory(),providerRegistry,"test", "INSIDE", plan.getId(),Collections.<String>emptySet(),"",plan,saved)); }
            catch (RuntimeException failure) { log(Level.WARN, "normal tick scheduler failed closed for plan=%s reason=%s", plan.getId(), failure.getClass().getSimpleName()); }
            log(Level.INFO, "armed plan tick=%d fixture=%s planning=%s plans=%d", tick, testExecutionFixture != null, configuration.isAutonomousPlanningEnabled(), saved.getNarrativePlans().size());
            executeEligibleTestFixture(tick,world,saved,scope,cycle == null || cycle.getPlan()==null ? plan : cycle.getPlan());
            return;
        }
    }

    private void evaluateAutonomousPlanning(long tick, WorldServer world, DirectorWorldSavedData saved,
            com.sobrenaturaldirector.observation.model.PlayerSnapshot player,
            com.sobrenaturaldirector.decision.model.DecisionContext context) {
        if (!configuration.isAutonomousPlanningEnabled()) return;
        RegionKey key = RegionKey.fromChunk(player.getDimension(), (int)Math.floor(player.getX()) >> 4, (int)Math.floor(player.getZ()) >> 4);
        String scope = player.getPlayerId().toString()+":"+player.getDimension()+":"+key.getX()+":"+key.getZ();
        com.sobrenaturaldirector.situation.SituationContext situation = new com.sobrenaturaldirector.situation.SituationContext(
                player.getDimension(), tick, key.getX(), key.getZ(), saved.getState().getDirectorSeed(), false, true, true,
                context.getEventConcurrency() == 0, context.getContentTags(), context.getEnvironmentProfile());
        com.sobrenaturaldirector.narrative.NarrativeArbitrationContext arbitration =
                new com.sobrenaturaldirector.narrative.NarrativeArbitrationContext(context,
                        new com.sobrenaturaldirector.situation.SituationMemory(), context.getContentTags(), null,
                        latestPacing.get(player.getPlayerId()), false, "", saved.getState().getDirectorSeed(), null);
        com.sobrenaturaldirector.narrative.PersistentNarrativePlan current = null;
        for (com.sobrenaturaldirector.narrative.PersistentNarrativePlan candidate : saved.getNarrativePlans())
            if (candidate.getDimension() == player.getDimension() && candidate.getRegionX() == key.getX()
                    && candidate.getRegionZ() == key.getZ() && !candidate.getState().isTerminal()) { current = candidate; break; }
        String fingerprint = scope+"|"+context.getContentTags()+"|"+context.getLocationTags()+"|"+context.getEventConcurrency();
        com.sobrenaturaldirector.narrative.ControlledAutonomousPlanningScheduler.PlanningCycleResult cycle = autonomousScheduler.evaluate(new com.sobrenaturaldirector.narrative.ControlledAutonomousPlanningScheduler.CycleInput(
                scope, tick, fingerprint, opportunityGenerator.generate(context, scope), arbitration, situation,
                new com.sobrenaturaldirector.situation.SituationMemory(), providerRegistry, "auto:"+scope, "INSIDE",
                fingerprint, context.getContentTags(), current == null ? "" : current.getThreadId(), current, saved));
        executeEligibleTestFixture(tick, world, saved, scope, cycle.getPlan());
    }

    private void executeEligibleTestFixture(long tick, WorldServer world, DirectorWorldSavedData saved, String scope,
            com.sobrenaturaldirector.narrative.PersistentNarrativePlan plan) {
        AutonomousExecutionTestFixture fixture=testExecutionFixture;
        if (fixture==null || plan==null || !fixture.isAuthorizedByEnvironment()) return;
        normalTickExecutionAttempts++; lastNormalTickPlanId=plan.getId();
        String executionId="execution:"+plan.getId();
        ExecutionAuthorization authorization=new ExecutionAuthorization(executionId, plan.getId(), plan.getPlanFingerprint(), scope,
                plan.getProviderRevision(), "runtime:"+world.provider.dimensionId, tick, DirectorRuntimeMode.CONTROLLED_EXECUTION, true);
        com.sobrenaturaldirector.situation.SituationContext situation=new com.sobrenaturaldirector.situation.SituationContext(
                plan.getDimension(), tick, plan.getRegionX(), plan.getRegionZ(), saved.getState().getDirectorSeed(),
                false, true, true, true, Collections.<String>emptySet(), null);
        com.sobrenaturaldirector.execution.ExecutionPreparationContext preparation =
                new com.sobrenaturaldirector.execution.ExecutionPreparationContext(world, saved, situation, null,
                        new com.sobrenaturaldirector.situation.SituationMemory(), fixture.x, fixture.y, fixture.z, executionId);
        SafeAutonomousExecutionCoordinator.Result result = autonomousExecution.execute(plan, preparation, authorization,
                configuration.isExecutionEnabled() && configuration.isAutonomousExecutionEnabled(), normalTickFactory(fixture, plan, authorization));
        log(Level.INFO, "normal tick execution plan=%s status=%s reason=%s", plan.getId(), result.getStatus(), result.getReason());
        lastNormalTickExecutionId=executionId;
        if (result.getStatus()==SafeAutonomousExecutionCoordinator.Status.ALREADY_HANDLED) normalTickDuplicateEffects++;
        if (result.getStatus()!=SafeAutonomousExecutionCoordinator.Status.EXECUTED) return;
        normalTickApplied++;
        if (!fixture.blockName.equals(Block.blockRegistry.getNameForObject(world.getBlock(fixture.x, fixture.y, fixture.z)))) return;
        String outcomeId="outcome:"+executionId; lastNormalTickOutcomeId=outcomeId;
        if (saved.getExecutionOutcome(outcomeId)==null) saved.recordExecutionOutcome(new ExecutionOutcome(outcomeId, executionId, plan.getId(), "minecraft", fixture.blockName+"@"+fixture.x+","+fixture.y+","+fixture.z, ExecutionOutcome.Status.CONFIRMED, tick, "normal tick physical confirmation", false));
        if (applyConfirmedOutcome(saved, outcomeId, scope)==ExecutionOutcomeFeedbackCoordinator.Status.APPLIED) normalTickFeedbackApplied++;
    }

    private com.sobrenaturaldirector.execution.PreparedExecutionPlanFactory normalTickFactory(final AutonomousExecutionTestFixture fixture,
            final com.sobrenaturaldirector.narrative.PersistentNarrativePlan plan, final ExecutionAuthorization authorization) {
        return new com.sobrenaturaldirector.execution.PreparedExecutionPlanFactory(Collections.singletonList(
                new com.sobrenaturaldirector.execution.ExecutionSlicePreparer() {
                    public boolean supports(com.sobrenaturaldirector.capability.CapabilityDescriptor capability) { return true; }
                    public com.sobrenaturaldirector.execution.ControlledProviderSlice prepare(
                            com.sobrenaturaldirector.capability.CandidatePlan candidate,
                            final com.sobrenaturaldirector.capability.CapabilityDescriptor capability,
                            final com.sobrenaturaldirector.execution.ExecutionPreparationContext context) {
                        final String id = capability.getProvider().getValue();
                        final com.sobrenaturaldirector.execution.CoordinatedSlicePhase phase =
                                capability.getId().getValue().equals(com.sobrenaturaldirector.capability.CapabilityVocabulary.STRUCTURE_SOURCE.getValue())
                                ? com.sobrenaturaldirector.execution.CoordinatedSlicePhase.STRUCTURE
                                : capability.getId().getValue().equals(com.sobrenaturaldirector.capability.CapabilityVocabulary.ACTOR_SOURCE.getValue())
                                ? com.sobrenaturaldirector.execution.CoordinatedSlicePhase.ACTOR
                                : com.sobrenaturaldirector.execution.CoordinatedSlicePhase.THREAT;
                        return new com.sobrenaturaldirector.execution.ControlledProviderSlice() {
                            public String getSliceId() { return "normal-tick:" + id; }
                            public com.sobrenaturaldirector.execution.CoordinatedSlicePhase getPhase() { return phase; }
                            public ProviderId getProviderId() { return capability.getProvider(); }
                            public com.sobrenaturaldirector.execution.CoordinatedSliceResult preflight() {
                                return com.sobrenaturaldirector.execution.CoordinatedSliceResult.of(com.sobrenaturaldirector.execution.CoordinatedSliceResult.Status.EXECUTED, getSliceId());
                            }
                            public com.sobrenaturaldirector.execution.CoordinatedSliceResult execute() {
                                if (phase != com.sobrenaturaldirector.execution.CoordinatedSlicePhase.STRUCTURE)
                                    return com.sobrenaturaldirector.execution.CoordinatedSliceResult.of(com.sobrenaturaldirector.execution.CoordinatedSliceResult.Status.EXECUTED, getSliceId());
                                MutationExecutionRequest request = new MutationExecutionRequest(context.getExecutionId(), plan.getId(), plan.getPlanFingerprint(),
                                        MutationOperation.PLACE_BLOCK, context.getSituation().getDimension(), fixture.x, fixture.y, fixture.z,
                                        fixture.blockName, fixture.metadata, fixture.expectedBlock);
                                MutationExecutionResult result = executeAuthorizedMutation(context.getWorld(), context.getSaved(), request,
                                        authorization);
                                log(Level.INFO, "normal tick policy target=%s air=%s belowSolid=%s expected=%s thread=%s fingerprintMatch=%s", Block.blockRegistry.getNameForObject(context.getWorld().getBlock(fixture.x, fixture.y, fixture.z)), context.getWorld().isAirBlock(fixture.x, fixture.y, fixture.z), context.getWorld().getBlock(fixture.x, fixture.y - 1, fixture.z).getMaterial().isSolid(), fixture.expectedBlock, Thread.currentThread().getName(), authorization.getPlanFingerprint().equals(request.getSourceId()));
                                return result.getStatus() == MutationExecutionResult.Status.EXECUTED
                                        ? com.sobrenaturaldirector.execution.CoordinatedSliceResult.of(com.sobrenaturaldirector.execution.CoordinatedSliceResult.Status.EXECUTED, getSliceId())
                                        : com.sobrenaturaldirector.execution.CoordinatedSliceResult.of(com.sobrenaturaldirector.execution.CoordinatedSliceResult.Status.FAILED, getSliceId());
                            }
                            public com.sobrenaturaldirector.execution.CoordinatedSliceResult reconcile() { return preflight(); }
                            public com.sobrenaturaldirector.execution.CoordinatedSliceResult compensate() { return preflight(); }
                        };
                    }
                }));
    }

    private void evaluate(long tick, WorldServer world, ObservationFrame frame, DirectorWorldState state,
            com.sobrenaturaldirector.decision.model.DecisionContext context) {
        DecisionResult decision = decisions.decide(context, state.getDirectorSeed());
        metrics.decision();
        if (decision.getSelected().getIntent() == Intent.NO_ACTION) {
            metrics.noAction();
            latest = result(tick, world.provider.dimensionId, frame, state, Intent.NO_ACTION,
                    null, 0, "NO_ACTION", "NO_ACTION", 0, "NO_ACTION");
            return;
        }
        PlanningContext planningContext = new PlanningContext(decision, context, Collections.<String, Boolean>emptyMap(),
                context.isSafetyKnown(), context.getBudgets(), tick);
        PlanningResult planned = planner.plan(planningContext);
        metrics.plan();
        DirectorPlan plan = planned.getPlan();
        if (plan == null) {
            latest = result(tick, world.provider.dimensionId, frame, state, decision.getSelected().getIntent(),
                    null, 0, "NO_PLAN", "NO_PLAN", 0, "PLAN_NOT_REQUIRED");
            return;
        }
        Map<String, ResolutionResult> resolutions = resolve(plan, world);
        CompositionResult composed = composition.compose(new CompositionRequest(plan, "generic", StructureScale.SMALL,
                state.getDirectorSeed(), resolutions));
        if (composed.getStatus().name().contains("INVALID")) metrics.compositionFailure();
        SpatialPlanningResult positioned = spatial.plan(plan.getPlanId(), composed.getStructure(), composed.getEncounters(),
                composed.getRewards(), SpatialBudget.TECHNICAL_COMPOSITION_DEFAULT, state.getDirectorSeed(), false);
        if (positioned.getStatus().name().contains("UNPLACEABLE")) metrics.spatialFailure();
        int count = 0;
        if (positioned.getPlan() != null) {
            MutationTransactionPlan mutation = mutations.plan(plan.getPlanId(), positioned.getPlan(),
                    new MutationBudget(1048576, 128, 1048576, 0, 256, 512, 128));
            sink.accept(mutation);
            metrics.dryRunMutationPlan();
            count = mutation.getIntents().size();
        }
        latest = result(tick, world.provider.dimensionId, frame, state, decision.getSelected().getIntent(),
                plan.getPlanId(), resolutions.size(), composed.getStatus().name(), positioned.getStatus().name(),
                count, "DRY_RUN_ONLY");
    }

    private Map<String, ResolutionResult> resolve(DirectorPlan plan, WorldServer world) {
        Map<String, ResolutionResult> result = new HashMap<String, ResolutionResult>();
        List<ProviderId> providers = new ArrayList<ProviderId>(catalog.getProviders().keySet());
        List<ContentKey> entities = new ArrayList<ContentKey>();
        for (com.sobrenaturaldirector.content.model.SemanticContentEntry entry : catalog.getEntries().values()) {
            if (entry.getKind() == ContentKind.ENTITY) entities.add(entry.getKey());
        }
        RuntimeContentIndex runtime = new ForgeContentAvailabilityProbe().snapshot(providers,
                Collections.<ContentKey>emptyList(), Collections.<ContentKey>emptyList(), entities);
        for (com.sobrenaturaldirector.planner.requirement.PlanRequirement requirement : plan.getRequirements()) {
            try {
                ResolutionRequest request = PlanRequirementResolution.requestFor(requirement);
                result.put(requirement.getId(), resolver.resolve(request, catalog, runtime));
            } catch (RuntimeException failure) {
                metrics.resolverFailure();
            }
        }
        return result;
    }

    private DirectorWorldSavedData state(WorldServer world) {
        DirectorWorldSavedData saved = (DirectorWorldSavedData) world.perWorldStorage.loadData(
                DirectorWorldSavedData.class, com.sobrenaturaldirector.persistence.DirectorPersistenceMetadata.DATA_NAME);
        if (saved == null) {
            saved = new DirectorWorldSavedData(com.sobrenaturaldirector.persistence.DirectorPersistenceMetadata.DATA_NAME);
            saved.replaceState(DirectorWorldState.empty(world.getSeed()));
            world.perWorldStorage.setData(com.sobrenaturaldirector.persistence.DirectorPersistenceMetadata.DATA_NAME, saved);
            log(Level.INFO, "DirectorWorldState created for dimension=%d seed=%d", world.provider.dimensionId, world.getSeed());
        } else {
            log(Level.INFO, "DirectorWorldState loaded for dimension=%d status=%s", world.provider.dimensionId,
                    saved.getLoadStatus().name());
        }
        return saved;
    }

    private static WorldServer overworld(MinecraftServer server) {
        if (server.worldServers == null) return null;
        for (WorldServer world : server.worldServers) if (world != null && world.provider.dimensionId == 0) return world;
        return null;
    }

    private DirectorWorldState worldStateFingerprint(WorldServer world) {
        try { return state(world).getState(); } catch (RuntimeException ignored) { return DirectorWorldState.empty(world.getSeed()); }
    }

    private DirectorDryRunResult result(long tick, int dimension, ObservationFrame frame, DirectorWorldState state,
            Intent decision, String plan, int resolved, String composition, String spatial, int count, String reason) {
        return result(tick, dimension, frame, state, decision, plan, resolved, composition, spatial, count, reason,
                fingerprint(frame.getCaptureId(), decision, plan));
    }

    private DirectorDryRunResult result(long tick, int dimension, ObservationFrame frame, DirectorWorldState state,
            Intent decision, String plan, int resolved, String composition, String spatial, int count,
            String reason, String fingerprint) {
        return new DirectorDryRunResult(tick, dimension, frame.getCaptureId(), state.hashCode(), decision, plan,
                resolved, composition, spatial, count, reason, fingerprint);
    }

    private static String fingerprint(String observation, Intent decision, String plan) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest((observation + ":" + decision + ":" + plan)
                    .getBytes(Charset.forName("UTF-8")));
            StringBuilder value = new StringBuilder();
            for (int i = 0; i < 8; i++) value.append(String.format("%02x", bytes[i] & 255));
            return value.toString();
        } catch (NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }

    public DirectorRuntimeMode getMode() { return DirectorRuntimeMode.DRY_RUN; }
    public DirectorDryRunResult getLatestResult() { return latest; }
    public com.sobrenaturaldirector.narrative.PacingAssessment getLatestPacing(java.util.UUID playerId) { return latestPacing.get(playerId); }
    public DirectorRuntimeMetrics getMetrics() { return metrics; }
    public DryRunMutationSink getMutationSink() { return sink; }
    public ControlledCompositionExecutor getControlledCompositionExecutor() { return controlledComposition; }
    public long getLastEvaluationTick() { return lastEvaluationTick; }
    public long getNextEvaluationTick() { return nextEvaluationTick; }
    public int getStateFingerprint() { return stateFingerprint; }
    public int getLastLifecycleProcessed() { return lastLifecycleProcessed; }
    public NarrativePlanLifecycleCoordinator getNarrativePlanLifecycle() { return lifecycle; }
    public com.sobrenaturaldirector.narrative.NarrativeThreadManager getNarrativeThreadManager() { return threads; }
    public SafeAutonomousExecutionCoordinator getAutonomousExecutionCoordinator() { return autonomousExecution; }
    public MutationExecutionResult executeAuthorizedMutation(WorldServer world, DirectorWorldSavedData saved,
            MutationExecutionRequest request, ExecutionAuthorization authorization) {
        return autonomousExecution.executeMutation(world, saved, request, authorization,
                configuration.isExecutionEnabled() && configuration.isAutonomousExecutionEnabled());
    }
    public ExecutionOutcomeFeedbackCoordinator.Status applyConfirmedOutcome(DirectorWorldSavedData saved, String outcomeId) {
         return outcomeFeedback.apply(saved, outcomeId);
    }
    public ExecutionOutcomeFeedbackCoordinator.Status applyConfirmedOutcome(DirectorWorldSavedData saved, String outcomeId, String scope) {
        return outcomeFeedback.apply(saved, outcomeId, scope, threads);
    }
    public void armTestExecutionFixture(int x, int y, int z, String blockName, int metadata, String expectedBlock) {
        if (!"GRANTED_BY_PROJECT_OWNER".equals(System.getenv("TEST_EXECUTION_AUTHORIZATION"))) { log(Level.WARN, "test execution fixture authorization absent"); return; }
        registerTestCapability(com.sobrenaturaldirector.capability.CapabilityVocabulary.STRUCTURE_SOURCE, "structure");
        registerTestCapability(com.sobrenaturaldirector.capability.CapabilityVocabulary.ACTOR_SOURCE, "actor");
        registerTestCapability(com.sobrenaturaldirector.capability.CapabilityVocabulary.THREAT_SOURCE, "threat");
        testExecutionFixture=new AutonomousExecutionTestFixture(x,y,z,blockName,metadata,expectedBlock);
        log(Level.INFO, "test execution fixture armed at %d,%d,%d", x,y,z);
    }
    public void armTestExecutionPlan(com.sobrenaturaldirector.narrative.PersistentNarrativePlan plan) {
        if (plan==null || !"GRANTED_BY_PROJECT_OWNER".equals(System.getenv("TEST_EXECUTION_AUTHORIZATION"))) return;
        testExecutionPlan=plan;
    }
    private void registerTestCapability(com.sobrenaturaldirector.content.model.SemanticCapability capability, String provider) {
        com.sobrenaturaldirector.content.model.ProviderId id=new com.sobrenaturaldirector.content.model.ProviderId(provider);
        providerRegistry.registerDescriptor(new com.sobrenaturaldirector.content.model.ProviderDescriptor(id,provider,com.sobrenaturaldirector.content.model.EvidenceStatus.CONFIRMED,true,com.sobrenaturaldirector.decision.model.ProviderStatus.AVAILABLE,false,Collections.<String>emptySet(),"test",Collections.singleton(new com.sobrenaturaldirector.capability.CapabilityDescriptor(capability,id,com.sobrenaturaldirector.capability.ProviderAvailability.RUNTIME_CONTROL,80,0)),Collections.<com.sobrenaturaldirector.capability.CapabilityPolicyBinding>emptySet(),com.sobrenaturaldirector.capability.ProviderAvailability.RUNTIME_CONTROL));
    }
    public String getLastNormalTickPlanId() { return lastNormalTickPlanId; }
    public String getLastNormalTickExecutionId() { return lastNormalTickExecutionId; }
    public String getLastNormalTickOutcomeId() { return lastNormalTickOutcomeId; }
    public int getNormalTickExecutionAttempts() { return normalTickExecutionAttempts; }
    public int getNormalTickApplied() { return normalTickApplied; }
    public int getNormalTickDuplicateEffects() { return normalTickDuplicateEffects; }
    public int getNormalTickFeedbackApplied() { return normalTickFeedbackApplied; }

    private static final class AutonomousExecutionTestFixture {
        private final int x,y,z,metadata; private final String blockName,expectedBlock;
        private AutonomousExecutionTestFixture(int x,int y,int z,String blockName,int metadata,String expectedBlock){this.x=x;this.y=y;this.z=z;this.blockName=blockName;this.metadata=metadata;this.expectedBlock=expectedBlock;}
        private boolean isAuthorizedByEnvironment(){return "GRANTED_BY_PROJECT_OWNER".equals(System.getenv("TEST_EXECUTION_AUTHORIZATION"));}
    }
    public EnvironmentObservationService getEnvironmentObservation() { return environment; }

    private static void log(Level level, String message, Object... arguments) {
        FMLLog.log("sobrenaturaldirector", level, message, arguments);
    }
}
