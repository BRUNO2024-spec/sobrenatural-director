package com.sobrenaturaldirector.benchmark;

import java.util.List;
import com.sobrenaturaldirector.decision.DecisionEngine;
import com.sobrenaturaldirector.decision.DecisionResult;
import com.sobrenaturaldirector.decision.model.CandidateAction;
import com.sobrenaturaldirector.decision.model.DecisionContext;

/** Adapter for the current production heuristic engine; benchmark mode is plan-only. */
public final class CurrentHeuristicDirectorPolicy implements DirectorDecisionPolicy {
    private final DecisionEngine engine = new DecisionEngine();
    public DecisionResult evaluate(DecisionContext context, List<CandidateAction> candidates, long seed) {
        return engine.decide(context, candidates, seed);
    }
}
