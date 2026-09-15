package com.sobrenaturaldirector.planner.replanning;
import com.sobrenaturaldirector.planner.model.*;
public final class Replanner {public ReplanResult replan(DirectorPlan plan,PlanFailure failure,int attempts){if(plan==null||failure==null)throw new IllegalArgumentException("invalid replan request");if(!failure.isRetryable()||attempts>=2)return new ReplanResult(ReplanOutcome.ABANDON,plan,"REPLAN_LIMIT_OR_NON_RETRYABLE");return new ReplanResult(ReplanOutcome.SUSPEND,plan,"RETRYABLE_FAILURE_DEFERRED");}}
