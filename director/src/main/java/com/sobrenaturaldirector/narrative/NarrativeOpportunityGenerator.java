package com.sobrenaturaldirector.narrative;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.decision.model.DecisionContext;
import com.sobrenaturaldirector.decision.model.Intent;

/** Cheap semantic opportunity generation; it never scans or mutates the world. */
public final class NarrativeOpportunityGenerator {
    public List<NarrativeOpportunity> generate(DecisionContext context, String scope) {
        if (context == null || scope == null) throw new IllegalArgumentException("opportunity inputs are required");
        if (!context.isSafetyKnown() || context.getEventConcurrency() > 1) return Collections.emptyList();
        if (context.getRecoveryNeed() > .8 || context.getFatigue() > .9) return Collections.emptyList();
        return Collections.singletonList(new NarrativeOpportunity("opportunity:" + scope + ":ambient",
                Intent.AMBIENT_HINT, .42, .7, Collections.<String>emptySet(),
                Arrays.asList("SEMANTIC_CONTEXT_ELIGIBLE"), false, null));
    }
}
