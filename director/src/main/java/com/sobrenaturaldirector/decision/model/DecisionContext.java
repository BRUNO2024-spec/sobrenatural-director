package com.sobrenaturaldirector.decision.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.sobrenaturaldirector.environment.model.SemanticRegionProfile;

/** Immutable, runtime-independent inputs for one decision cycle. */
public final class DecisionContext {
    private final long tick, worldAge;
    private final double tension, pressure, fatigue, recoveryNeed, healthRatio, combatPower, isolation;
    private final boolean underground, observingSite, safetyKnown;
    private final int eventConcurrency;
    private final Map<String, Double> budgets;
    private final Map<String, ProviderStatus> providers;
    private final Set<String> contentTags, locationTags, uniqueClaims;
    private final Map<String, Long> cooldownUntil;
    private final SemanticRegionProfile environmentProfile;

    public DecisionContext(long tick, long worldAge, double tension, double pressure, double fatigue,
            double recoveryNeed, double healthRatio, double combatPower, double isolation,
            boolean underground, boolean observingSite, boolean safetyKnown, int eventConcurrency,
            Map<String, Double> budgets, Map<String, ProviderStatus> providers, Set<String> contentTags,
            Set<String> locationTags, Set<String> uniqueClaims, Map<String, Long> cooldownUntil) {
        this(tick, worldAge, tension, pressure, fatigue, recoveryNeed, healthRatio, combatPower, isolation, underground, observingSite, safetyKnown, eventConcurrency, budgets, providers, contentTags, locationTags, uniqueClaims, cooldownUntil, null);
    }
    public DecisionContext(long tick, long worldAge, double tension, double pressure, double fatigue,
            double recoveryNeed, double healthRatio, double combatPower, double isolation,
            boolean underground, boolean observingSite, boolean safetyKnown, int eventConcurrency,
            Map<String, Double> budgets, Map<String, ProviderStatus> providers, Set<String> contentTags,
            Set<String> locationTags, Set<String> uniqueClaims, Map<String, Long> cooldownUntil,
            SemanticRegionProfile environmentProfile) {
        this.tick = tick; this.worldAge = worldAge; this.tension = finite01(tension, "tension");
        this.pressure = finite01(pressure, "pressure"); this.fatigue = finite01(fatigue, "fatigue");
        this.recoveryNeed = finite01(recoveryNeed, "recoveryNeed"); this.healthRatio = finite01(healthRatio, "healthRatio");
        this.combatPower = finite01(combatPower, "combatPower"); this.isolation = finite01(isolation, "isolation");
        if (eventConcurrency < 0) throw new IllegalArgumentException("eventConcurrency must be non-negative");
        this.underground = underground; this.observingSite = observingSite; this.safetyKnown = safetyKnown;
        this.eventConcurrency = eventConcurrency;
        this.budgets = nonNegativeMap(budgets, "budget"); this.providers = copy(providers);
        this.contentTags = set(contentTags); this.locationTags = set(locationTags); this.uniqueClaims = set(uniqueClaims);
        this.cooldownUntil = copy(cooldownUntil);
        this.environmentProfile = environmentProfile;
    }
    private static double finite01(double v, String name) { if (Double.isNaN(v) || Double.isInfinite(v) || v < 0 || v > 1) throw new IllegalArgumentException(name + " must be finite in [0,1]"); return v; }
    private static <K,V> Map<K,V> copy(Map<K,V> value) { return Collections.unmodifiableMap(new LinkedHashMap<K,V>(value == null ? Collections.<K,V>emptyMap() : value)); }
    private static <K> Set<K> set(Set<K> value) { return Collections.unmodifiableSet(new LinkedHashSet<K>(value == null ? Collections.<K>emptySet() : value)); }
    private static Map<String, Double> nonNegativeMap(Map<String, Double> value, String name) { Map<String, Double> result = new LinkedHashMap<String, Double>(); if (value != null) for (Map.Entry<String, Double> e : value.entrySet()) { if (e.getValue() == null || Double.isNaN(e.getValue()) || Double.isInfinite(e.getValue()) || e.getValue() < 0) throw new IllegalArgumentException(name + " must be finite and non-negative"); result.put(e.getKey(), e.getValue()); } return Collections.unmodifiableMap(result); }
    public static DecisionContext calm(long tick) { return new DecisionContext(tick, tick, 0, 0, 0, 0, 1, .5, 0, false, false, true, 1, Collections.singletonMap("threat", 10.0), Collections.singletonMap("HORROR", ProviderStatus.AVAILABLE), Collections.singleton("COMMON_THREAT"), Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String, Long>emptyMap()); }
    public long getTick(){return tick;} public long getWorldAge(){return worldAge;} public double getTension(){return tension;} public double getPressure(){return pressure;} public double getFatigue(){return fatigue;} public double getRecoveryNeed(){return recoveryNeed;} public double getHealthRatio(){return healthRatio;} public double getCombatPower(){return combatPower;} public double getIsolation(){return isolation;} public boolean isUnderground(){return underground;} public boolean isObservingSite(){return observingSite;} public boolean isSafetyKnown(){return safetyKnown;} public int getEventConcurrency(){return eventConcurrency;} public Map<String,Double> getBudgets(){return budgets;} public Map<String,ProviderStatus> getProviders(){return providers;} public Set<String> getContentTags(){return contentTags;} public Set<String> getLocationTags(){return locationTags;} public Set<String> getUniqueClaims(){return uniqueClaims;} public Map<String,Long> getCooldownUntil(){return cooldownUntil;}
    public SemanticRegionProfile getEnvironmentProfile(){return environmentProfile;}
    public DecisionContext withEnvironmentProfile(SemanticRegionProfile profile){return new DecisionContext(tick,worldAge,tension,pressure,fatigue,recoveryNeed,healthRatio,combatPower,isolation,underground,observingSite,safetyKnown,eventConcurrency,budgets,providers,contentTags,locationTags,uniqueClaims,cooldownUntil,profile);}
}
