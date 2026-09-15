package com.sobrenaturaldirector.situation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SituationGoalCandidate {
    private final SituationGoal goal;
    private final int score;
    private final boolean satisfiable;
    private final List<String> reasons;
    public SituationGoalCandidate(SituationGoal goal, int score, boolean satisfiable, List<String> reasons) { this.goal = goal; this.score = score; this.satisfiable = satisfiable; this.reasons = Collections.unmodifiableList(new ArrayList<String>(reasons)); }
    public SituationGoal getGoal() { return goal; } public int getScore() { return score; } public boolean isSatisfiable() { return satisfiable; } public List<String> getReasons() { return reasons; }
}
