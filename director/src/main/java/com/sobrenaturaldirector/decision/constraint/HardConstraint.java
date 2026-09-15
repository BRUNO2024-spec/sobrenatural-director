package com.sobrenaturaldirector.decision.constraint;
import com.sobrenaturaldirector.decision.model.CandidateAction;
import com.sobrenaturaldirector.decision.model.DecisionContext;
public interface HardConstraint { ConstraintResult evaluate(DecisionContext context, CandidateAction candidate); }
