package com.sobrenaturaldirector.situation;

import com.sobrenaturaldirector.content.model.ProviderId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SituationMemoryEntry {
    private final String id, combination;
    private final SituationGoal goal;
    private final long tick;
    private final int regionX, regionZ;
    private final List<ProviderId> providers;
    private final int score;
    public SituationMemoryEntry(String id, SituationGoal goal, long tick, int regionX, int regionZ, String combination, List<ProviderId> providers, int score) {
        if (id == null || goal == null || combination == null || tick < 0) throw new IllegalArgumentException("invalid situation memory");
        this.id = id; this.goal = goal; this.tick = tick; this.regionX = regionX; this.regionZ = regionZ; this.combination = combination; this.score = score;
        this.providers = Collections.unmodifiableList(new ArrayList<ProviderId>(providers == null ? Collections.<ProviderId>emptyList() : providers));
    }
    public String getId() { return id; } public SituationGoal getGoal() { return goal; } public long getTick() { return tick; }
    public int getRegionX() { return regionX; } public int getRegionZ() { return regionZ; }
    public String getCombination() { return combination; } public List<ProviderId> getProviders() { return providers; } public int getScore() { return score; }
}
