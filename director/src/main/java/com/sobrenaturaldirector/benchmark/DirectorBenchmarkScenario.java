package com.sobrenaturaldirector.benchmark;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable serializable scenario envelope for V1 and future policy adapters. */
public final class DirectorBenchmarkScenario {
    private final String benchmarkScenarioId;
    private final long seed;
    private final int dimension;
    private final String regionKey;
    private final String environmentClassification;
    private final Map<String, String> state;

    public DirectorBenchmarkScenario(String benchmarkScenarioId, long seed, int dimension, String regionKey,
            String environmentClassification, Map<String, String> state) {
        if (benchmarkScenarioId == null || benchmarkScenarioId.length() == 0 || regionKey == null || environmentClassification == null)
            throw new IllegalArgumentException("invalid benchmark scenario");
        this.benchmarkScenarioId = benchmarkScenarioId; this.seed = seed; this.dimension = dimension; this.regionKey = regionKey;
        this.environmentClassification = environmentClassification;
        this.state = Collections.unmodifiableMap(new LinkedHashMap<String, String>(state == null ? Collections.<String, String>emptyMap() : state));
    }
    public String getBenchmarkScenarioId() { return benchmarkScenarioId; }
    public long getSeed() { return seed; }
    public int getDimension() { return dimension; }
    public String getRegionKey() { return regionKey; }
    public String getEnvironmentClassification() { return environmentClassification; }
    public Map<String, String> getState() { return state; }
    public String fingerprint() {
        try { byte[] digest = MessageDigest.getInstance("SHA-256").digest((benchmarkScenarioId + "|" + seed + "|" + dimension + "|" + regionKey + "|" + environmentClassification + "|" + new ArrayList<String>(state.keySet())).getBytes(Charset.forName("UTF-8"))); StringBuilder result = new StringBuilder(); for (int i = 0; i < 16; i++) result.append(String.format("%02x", digest[i] & 255)); return result.toString(); }
        catch (Exception failure) { throw new IllegalStateException(failure); }
    }
}
