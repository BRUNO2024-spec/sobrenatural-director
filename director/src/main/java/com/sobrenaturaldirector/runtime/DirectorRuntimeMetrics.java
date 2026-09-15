package com.sobrenaturaldirector.runtime;

public final class DirectorRuntimeMetrics {
    private long evaluations;
    private long noAction;
    private long decisions;
    private long plansProduced;
    private long resolverFailures;
    private long compositionFailures;
    private long spatialFailures;
    private long dryRunMutationPlans;
    private long exceptions;

    public void evaluation() { evaluations++; }
    public void noAction() { noAction++; }
    public void decision() { decisions++; }
    public void plan() { plansProduced++; }
    public void resolverFailure() { resolverFailures++; }
    public void compositionFailure() { compositionFailures++; }
    public void spatialFailure() { spatialFailures++; }
    public void dryRunMutationPlan() { dryRunMutationPlans++; }
    public void exception() { exceptions++; }
    public long getEvaluations() { return evaluations; }
    public long getNoAction() { return noAction; }
    public long getDecisions() { return decisions; }
    public long getPlansProduced() { return plansProduced; }
    public long getResolverFailures() { return resolverFailures; }
    public long getCompositionFailures() { return compositionFailures; }
    public long getSpatialFailures() { return spatialFailures; }
    public long getDryRunMutationPlans() { return dryRunMutationPlans; }
    public long getExceptions() { return exceptions; }
}
