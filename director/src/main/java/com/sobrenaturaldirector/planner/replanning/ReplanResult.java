package com.sobrenaturaldirector.planner.replanning;
import com.sobrenaturaldirector.planner.model.DirectorPlan;
public final class ReplanResult {private final ReplanOutcome outcome;private final DirectorPlan plan;private final String reason;public ReplanResult(ReplanOutcome o,DirectorPlan p,String r){outcome=o;plan=p;reason=r;}public ReplanOutcome getOutcome(){return outcome;}public DirectorPlan getPlan(){return plan;}public String getReason(){return reason;}}
