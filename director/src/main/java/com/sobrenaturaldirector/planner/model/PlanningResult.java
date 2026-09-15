package com.sobrenaturaldirector.planner.model;
import com.sobrenaturaldirector.planner.trace.PlanningTrace;
public final class PlanningResult {private final PlanningResultStatus status;private final DirectorPlan plan;private final PlanningTrace trace;public PlanningResult(PlanningResultStatus status,DirectorPlan plan,PlanningTrace trace){this.status=status;this.plan=plan;this.trace=trace;}public PlanningResultStatus getStatus(){return status;}public DirectorPlan getPlan(){return plan;}public PlanningTrace getTrace(){return trace;}}
