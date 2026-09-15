package com.sobrenaturaldirector.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.content.model.SemanticCapability;

/** Small diagnostic trace for deterministic planner decisions. */
public final class DecisionTrace {
    private final List<String> events = new ArrayList<String>();
    public void candidates(SemanticCapability requirement, List<CapabilityDescriptor> values) {
        events.add("candidates " + requirement + " " + providers(values));
    }
    public void selected(SemanticCapability requirement, CapabilityDescriptor value) {
        events.add("selected " + requirement + " " + value.getProvider() + " score=" + value.score());
    }
    public void rejected(String reason) { events.add("rejected reason=" + reason); }
    public void context(String summary) { events.add("context " + summary); }
    public void goal(String name, int score, List<String> reasons, boolean satisfiable) { events.add("goal " + name + " score=" + score + " satisfiable=" + satisfiable + " " + reasons); }
    public void requirements(String goal, List<SemanticCapability> values) { events.add("requirements " + goal + " " + values); }
    public void blueprint(String id, List<?> requiredRoles, List<?> optionalRoles, String intensity, int score, List<String> reasons, boolean feasible) { events.add("blueprint " + id + " roles=" + requiredRoles + " optional=" + optionalRoles + " intensity=" + intensity + " score=" + score + " feasible=" + feasible + " " + reasons); }
    public void optionalSkipped(Object role, Object capability) { events.add("optional element skipped role=" + role + " reason=missing capability " + capability); }
    public void optionalOmittedByPacing(Object role) { events.add("optional element skipped role=" + role + " reason=pacing suppressed"); }
    public void pacing(String blueprint, com.sobrenaturaldirector.situation.ThreatNarrativeAssessment assessment) { events.add("pacing blueprint=" + blueprint + " eligible=" + assessment.isEligible() + " modifier=" + assessment.getScoreModifier() + " recent=" + assessment.getRecentCount() + " sameRegion=" + assessment.getSameRegionCount() + " reasons=" + assessment.getReasons()); }
    public List<String> getEvents() { return Collections.unmodifiableList(new ArrayList<String>(events)); }
    private static String providers(List<CapabilityDescriptor> values) {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) { if (i > 0) result.append(','); CapabilityDescriptor value = values.get(i); result.append(value.getProvider()).append(" score=").append(value.score()); }
        return result.append(']').toString();
    }
}
