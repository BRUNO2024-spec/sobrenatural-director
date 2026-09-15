package com.sobrenaturaldirector.situation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BlueprintCandidate {
    private final SituationBlueprint blueprint;
    private final int score;
    private final boolean feasible;
    private final List<String> reasons;
    public BlueprintCandidate(SituationBlueprint blueprint, int score, boolean feasible, List<String> reasons) { this.blueprint = blueprint; this.score = score; this.feasible = feasible; this.reasons = Collections.unmodifiableList(new ArrayList<String>(reasons)); }
    public SituationBlueprint getBlueprint() { return blueprint; } public int getScore() { return score; } public boolean isFeasible() { return feasible; } public List<String> getReasons() { return reasons; }
}
