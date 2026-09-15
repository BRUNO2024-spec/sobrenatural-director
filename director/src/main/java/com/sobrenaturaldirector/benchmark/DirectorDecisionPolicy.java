package com.sobrenaturaldirector.benchmark;

import java.util.List;
import com.sobrenaturaldirector.decision.DecisionResult;
import com.sobrenaturaldirector.decision.model.CandidateAction;
import com.sobrenaturaldirector.decision.model.DecisionContext;

/** Neutral offline policy boundary. Hard safety remains outside future learned policies. */
public interface DirectorDecisionPolicy {
    DecisionResult evaluate(DecisionContext context, List<CandidateAction> candidates, long seed);
}
