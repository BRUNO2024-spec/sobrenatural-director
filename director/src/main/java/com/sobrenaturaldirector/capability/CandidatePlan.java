package com.sobrenaturaldirector.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import com.sobrenaturaldirector.content.model.ProviderId;
import com.sobrenaturaldirector.content.model.SemanticCapability;

public final class CandidatePlan {
    private final SituationIntent intent;
    private final Map<SemanticCapability, CapabilityDescriptor> selections;
    private final List<String> satisfiedConstraints;
    private final int score;
    public CandidatePlan(SituationIntent intent, Map<SemanticCapability, CapabilityDescriptor> selections, List<String> satisfiedConstraints) {
        this.intent = intent; this.selections = Collections.unmodifiableMap(new LinkedHashMap<SemanticCapability, CapabilityDescriptor>(selections));
        this.satisfiedConstraints = Collections.unmodifiableList(new ArrayList<String>(satisfiedConstraints));
        int value = 0; for (CapabilityDescriptor descriptor : selections.values()) value += descriptor.score(); this.score = value;
    }
    public SituationIntent getIntent() { return intent; }
    public Map<SemanticCapability, CapabilityDescriptor> getSelections() { return selections; }
    public List<String> getSatisfiedConstraints() { return satisfiedConstraints; }
    public int getScore() { return score; }
    public boolean isComplete() { return selections.keySet().containsAll(intent.getRequirements()); }
    public List<ProviderId> getProviders() { TreeSet<ProviderId> ids = new TreeSet<ProviderId>(); for (CapabilityDescriptor d : selections.values()) ids.add(d.getProvider()); return Collections.unmodifiableList(new ArrayList<ProviderId>(ids)); }
    public String signature() { StringBuilder value = new StringBuilder(intent.getId()); for (Map.Entry<SemanticCapability, CapabilityDescriptor> e : selections.entrySet()) value.append('|').append(e.getKey()).append('=').append(e.getValue().getProvider()); return value.toString(); }
}
