package com.sobrenaturaldirector.situation;

import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.content.model.SemanticCapability;

public final class SituationDecision {
    private final SituationContext context;
    private final List<SituationGoalCandidate> candidates;
    private final SituationGoalCandidate selected;
    private final List<SemanticCapability> requirements;
    private final CandidatePlan plan;
    private final BlueprintCandidate selectedBlueprint;
    private final List<BlueprintCandidate> blueprints;
    public SituationDecision(SituationContext context, List<SituationGoalCandidate> candidates, SituationGoalCandidate selected, List<SemanticCapability> requirements, CandidatePlan plan) { this(context, candidates, selected, requirements, plan, null, Collections.<BlueprintCandidate>emptyList()); }
    public SituationDecision(SituationContext context, List<SituationGoalCandidate> candidates, SituationGoalCandidate selected, List<SemanticCapability> requirements, CandidatePlan plan, BlueprintCandidate selectedBlueprint, List<BlueprintCandidate> blueprints) { this.context = context; this.candidates = Collections.unmodifiableList(candidates); this.selected = selected; this.requirements = Collections.unmodifiableList(requirements); this.plan = plan; this.selectedBlueprint = selectedBlueprint; this.blueprints = Collections.unmodifiableList(blueprints); }
    public SituationContext getContext() { return context; } public List<SituationGoalCandidate> getCandidates() { return candidates; }
    public SituationGoalCandidate getSelected() { return selected; } public List<SemanticCapability> getRequirements() { return requirements; } public CandidatePlan getPlan() { return plan; }
    public BlueprintCandidate getSelectedBlueprint() { return selectedBlueprint; } public List<BlueprintCandidate> getBlueprints() { return blueprints; }
    public boolean isPlanOnly() { return true; }
}
