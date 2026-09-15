package com.sobrenaturaldirector.decision.utility;
import com.sobrenaturaldirector.decision.model.CandidateAction; import com.sobrenaturaldirector.decision.model.DecisionContext;
public interface UtilityFactor { String getFactorId(); FactorResult evaluate(DecisionContext context,CandidateAction candidate); }
