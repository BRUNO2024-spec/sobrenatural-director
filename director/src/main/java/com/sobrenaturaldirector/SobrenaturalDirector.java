package com.sobrenaturaldirector;

import com.sobrenaturaldirector.bootstrap.BootstrapState;
import com.sobrenaturaldirector.config.FoundationConfig;
import com.sobrenaturaldirector.persistence.DirectorPersistenceMetadata;
import com.sobrenaturaldirector.observation.runtime.ObservationCoordinator;
import com.sobrenaturaldirector.runtime.DirectorRuntimeCoordinator;
import com.sobrenaturaldirector.runtime.DirectorRuntimeTickHandler;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.provider.ProviderBootstrap;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.Logger;
import com.sobrenaturaldirector.execution.PreparedExecutionPlanFactory;
import com.sobrenaturaldirector.execution.PreparedMultiProviderPlan;
import com.sobrenaturaldirector.execution.ExecutionPreparationContext;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.research.DirectorConsentCommand;
import com.sobrenaturaldirector.research.SessionConsentRegistry;
import com.sobrenaturaldirector.shadow.LearnedScorerModelLoader;
import com.sobrenaturaldirector.shadow.ShadowObservationService;
import com.sobrenaturaldirector.shadow.JsonlShadowEventWriter;
import java.io.File;

@Mod(modid = SobrenaturalDirector.MOD_ID, name = SobrenaturalDirector.MOD_NAME,
        version = SobrenaturalDirector.VERSION, acceptedMinecraftVersions = "[1.7.10]")
public final class SobrenaturalDirector {
    public static final String MOD_ID = "sobrenaturaldirector";
    public static final String MOD_NAME = "Sobrenatural Director";
    public static final String VERSION = "0.12.0-alpha";
    public static final String MINECRAFT_VERSION = "1.7.10";
    public static final String TARGET_FORGE_VERSION = "10.13.4.1614";

    private static Logger logger;
    private static BootstrapState state = BootstrapState.CONSTRUCTED;
    private static FoundationConfig configuration;
    private static ObservationCoordinator observationCoordinator;
    private static DirectorRuntimeCoordinator runtimeCoordinator;
    private static DirectorProviderRegistry providerRegistry;
    private static final SessionConsentRegistry consentRegistry = new SessionConsentRegistry();
    private static ShadowObservationService shadowService;
    private boolean observationRegistered;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        configuration = FoundationConfig.load(event.getSuggestedConfigurationFile(), logger);
        state = BootstrapState.PRE_INITIALIZED;
        logger.info("{} {} foundation initialized; gameplay is not enabled in this implementation phase.", MOD_NAME, VERSION);
        logger.info("Foundation configuration loaded (enabled={}, debug={}), schema={}",
                configuration.isEnabled(), configuration.getDebugLevel(), DirectorPersistenceMetadata.CURRENT_SCHEMA_VERSION);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        state = BootstrapState.INITIALIZED;
        observationCoordinator = new ObservationCoordinator(configuration);
        FMLCommonHandler.instance().bus().register(this);
        if (configuration.isShadowEnabled()) {
            LearnedScorerModelLoader.LoadedModel loaded = LearnedScorerModelLoader.load(new File(configuration.getShadowModelPath()), configuration.getShadowExpectedModelSha256(), configuration.getShadowExpectedFrozenConfigSha256(), configuration.getShadowExpectedFeatureSchemaSha256());
            logger.info("Shadow model status={}, shaPrefix={}", loaded.getStatus(), safePrefix(configuration.getShadowExpectedModelSha256()));
            if (loaded.getStatus() == com.sobrenaturaldirector.shadow.ShadowModelStatus.READY) {
                shadowService = new ShadowObservationService(loaded.getScorer(), new JsonlShadowEventWriter(new File("shadow-data"), 4L * 1024L * 1024L, 256L * 1024L * 1024L), configuration.getShadowQueueCapacity(), new com.sobrenaturaldirector.research.ResearchCollectionGate() { public boolean canCollect() { return configuration.isShadowCollectExperienceEnabled() && consentRegistry.allActiveAccepted(); } });
                shadowService.start();
            }
        }
        providerRegistry = new DirectorProviderRegistry();
        com.sobrenaturaldirector.provider.DirectorContentProvider customNpcs = ProviderBootstrap.registerAll(providerRegistry, configuration);
        logger.info("Optional providers registered; latest provider status={}, version={}", customNpcs.getStatus(), customNpcs.getDetectedVersion());
        runtimeCoordinator = new DirectorRuntimeCoordinator(observationCoordinator, configuration, providerRegistry, shadowService);
        if (!observationRegistered) {
            FMLCommonHandler.instance().bus().register(new DirectorRuntimeTickHandler(observationCoordinator,
                    runtimeCoordinator));
            observationRegistered = true;
        }
        logger.info("Foundation bootstrap reached INIT; no gameplay registrations were performed.");
        logger.info("Observation subsystem initialized in read-only transient mode (interval={} ticks).", configuration.getObservationIntervalTicks());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        state = BootstrapState.POST_INITIALIZED;
        logger.info("Foundation bootstrap reached POST_INIT; gameplay remains disabled.");
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) { ProviderBootstrap.onServerStarted(providerRegistry); }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) { event.registerServerCommand(new DirectorConsentCommand(consentRegistry)); }

    @Mod.EventHandler
    public void serverStopping(cpw.mods.fml.common.event.FMLServerStoppingEvent event) { if(shadowService!=null)shadowService.stop(); consentRegistry.clear(); }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) { consentRegistry.join(event.player.getCommandSenderName()); event.player.addChatMessage(new net.minecraft.util.ChatComponentText("[Director] Pesquisa Shadow: registre apenas decisões do sistema e outcomes objetivos. Sem consentimento não há coleta. Use /director consent accept ou /director consent decline.")); }

    @SubscribeEvent
    public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) { consentRegistry.disconnect(event.player.getCommandSenderName()); }

    public static BootstrapState getBootstrapState() { return state; }
    public static FoundationConfig getConfiguration() { return configuration; }
    public static ObservationCoordinator getObservationCoordinator() { return observationCoordinator; }
    public static DirectorRuntimeCoordinator getRuntimeCoordinator() { return runtimeCoordinator; }
    public static DirectorProviderRegistry getProviderRegistry() { return providerRegistry; }
    private static String safePrefix(String sha) { return sha == null || sha.length() < 8 ? "none" : sha.substring(0, 8); }
    /** High-level production preparation entry point used by validation and future controlled callers. */
    public static PreparedMultiProviderPlan prepareMultiProviderExecution(CandidatePlan plan, ExecutionPreparationContext context, String fingerprint) {
        if (providerRegistry == null) throw new IllegalStateException("provider registry is not initialized");
        PreparedExecutionPlanFactory factory = ProviderBootstrap.executionFactory(providerRegistry);
        return factory.prepare(plan, context, fingerprint);
    }
}
