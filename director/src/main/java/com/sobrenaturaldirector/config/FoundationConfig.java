package com.sobrenaturaldirector.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

public final class FoundationConfig {
    public static final int CURRENT_CONFIG_VERSION = 1;
    public static final int MIN_OBSERVATION_INTERVAL_TICKS = 20;
    public static final int MAX_OBSERVATION_INTERVAL_TICKS = 1200;
    private static final boolean DEFAULT_ENABLED = true;
    private static final DebugLevel DEFAULT_DEBUG_LEVEL = DebugLevel.INFO;

    private final boolean enabled;
    private final DebugLevel debugLevel;
    private final boolean observationEnabled;
    private final int observationIntervalTicks;
    private final boolean executionEnabled;
    private final boolean autonomousExecutionEnabled;
    private final boolean autonomousPlanningEnabled;
    private final boolean customNpcsProviderEnabled;

    private FoundationConfig(boolean enabled, DebugLevel debugLevel, boolean observationEnabled, int observationIntervalTicks,
            boolean executionEnabled, boolean autonomousExecutionEnabled, boolean autonomousPlanningEnabled, boolean customNpcsProviderEnabled) {
        this.enabled = enabled;
        this.debugLevel = debugLevel;
        this.observationEnabled = observationEnabled;
        this.observationIntervalTicks = observationIntervalTicks;
        this.executionEnabled = executionEnabled;
        this.autonomousExecutionEnabled = autonomousExecutionEnabled;
        this.autonomousPlanningEnabled = autonomousPlanningEnabled;
        this.customNpcsProviderEnabled = customNpcsProviderEnabled;
    }

    public static FoundationConfig load(File file, Logger logger) {
        Configuration config = new Configuration(file);
        try {
            config.load();
            boolean enabled = config.getBoolean("enabled", "general", DEFAULT_ENABLED,
                    "Foundation master flag; this phase has no gameplay regardless of its value.");
            String level = config.getString("level", "debug", DEFAULT_DEBUG_LEVEL.name(),
                    "Foundation logging level: OFF, ERROR, INFO, DECISIONS or VERBOSE.");
            DebugLevel debug = DebugLevel.parse(level);
            if (!debug.name().equalsIgnoreCase(level)) {
                logger.warn("Invalid debug.level '{}'; using {}.", level, DEFAULT_DEBUG_LEVEL);
            }
            config.get("general", "configVersion", CURRENT_CONFIG_VERSION,
                    "Foundation configuration schema version.").set(CURRENT_CONFIG_VERSION);
            boolean observation = config.getBoolean("enabled", "observation", true,
                    "Read-only observation capture; this phase never makes gameplay decisions.");
            int interval = config.getInt("intervalTicks", "observation", 200, MIN_OBSERVATION_INTERVAL_TICKS,
                    MAX_OBSERVATION_INTERVAL_TICKS, "Technical observation pulse interval.");
            boolean execution = config.getBoolean("enabled", "execution", false,
                    "Controlled validation only; automatic Director plans remain dry-run.");
            boolean autonomousExecution = config.getBoolean("enabled", "autonomousExecution", false,
                    "Opt-in bounded autonomous execution; requires explicit runtime authorization and final safety validation.");
            boolean autonomousPlanning = config.getBoolean("enabled", "autonomousPlanning", true,
                    "Run bounded semantic planning only; never executes gameplay.");
            boolean customNpcs = config.getBoolean("enabled", "providers.customnpcs", true,
                    "Discover optional CustomNPCs provider; does not enable mutations or automatic execution.");
            return new FoundationConfig(enabled, debug, observation, interval, execution, autonomousExecution, autonomousPlanning, customNpcs);
        } catch (RuntimeException exception) {
            logger.error("Foundation configuration failed; using safe defaults.", exception);
            return defaults();
        } finally {
            if (config.hasChanged()) config.save();
        }
    }

    public static FoundationConfig defaults() { return new FoundationConfig(DEFAULT_ENABLED, DEFAULT_DEBUG_LEVEL, true, 200, false, false, true, true); }
    public boolean isEnabled() { return enabled; }
    public DebugLevel getDebugLevel() { return debugLevel; }
    public boolean isObservationEnabled() { return observationEnabled; }
    public int getObservationIntervalTicks() { return observationIntervalTicks; }
    public boolean isExecutionEnabled() { return executionEnabled; }
    public boolean isAutonomousExecutionEnabled() { return autonomousExecutionEnabled; }
    public boolean isAutonomousPlanningEnabled() { return autonomousPlanningEnabled; }
    public boolean isCustomNpcsProviderEnabled() { return customNpcsProviderEnabled; }
}
