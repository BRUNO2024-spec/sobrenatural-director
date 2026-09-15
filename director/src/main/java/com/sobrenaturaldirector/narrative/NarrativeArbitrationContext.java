package com.sobrenaturaldirector.narrative;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import com.sobrenaturaldirector.decision.model.DecisionContext;
import com.sobrenaturaldirector.situation.SituationMemory;
import com.sobrenaturaldirector.situation.ThreatNarrativeAssessment;

public final class NarrativeArbitrationContext {
    private final DecisionContext decision; private final SituationMemory memory; private final Set<String> capabilities; private final ThreatNarrativeAssessment pacing; private final PacingAssessment adaptivePacing; private final boolean activeNarrative; private final String activeIntent; private final long seed; private final NarrativeThreadArbitrationContext thread;
    public NarrativeArbitrationContext(DecisionContext decision,SituationMemory memory,Set<String> capabilities,ThreatNarrativeAssessment pacing,boolean activeNarrative,String activeIntent,long seed){this(decision,memory,capabilities,pacing,activeNarrative,activeIntent,seed,null);}
    public NarrativeArbitrationContext(DecisionContext decision,SituationMemory memory,Set<String> capabilities,ThreatNarrativeAssessment pacing,boolean activeNarrative,String activeIntent,long seed,NarrativeThreadArbitrationContext thread){this(decision,memory,capabilities,pacing,null,activeNarrative,activeIntent,seed,thread);}
    public NarrativeArbitrationContext(DecisionContext decision,SituationMemory memory,Set<String> capabilities,ThreatNarrativeAssessment pacing,PacingAssessment adaptivePacing,boolean activeNarrative,String activeIntent,long seed,NarrativeThreadArbitrationContext thread){if(decision==null||seed<0)throw new IllegalArgumentException("invalid arbitration context");this.decision=decision;this.memory=memory==null?new SituationMemory():memory;this.capabilities=Collections.unmodifiableSet(new LinkedHashSet<String>(capabilities==null?Collections.<String>emptySet():capabilities));this.pacing=pacing;this.adaptivePacing=adaptivePacing;this.activeNarrative=activeNarrative;this.activeIntent=activeIntent==null?"":activeIntent;this.seed=seed;this.thread=thread;}
    public DecisionContext getDecision(){return decision;} public SituationMemory getMemory(){return memory;} public Set<String> getCapabilities(){return capabilities;} public ThreatNarrativeAssessment getPacing(){return pacing;} public PacingAssessment getAdaptivePacing(){return adaptivePacing;} public boolean hasActiveNarrative(){return activeNarrative;} public String getActiveIntent(){return activeIntent;} public long getSeed(){return seed;} public NarrativeThreadArbitrationContext getThread(){return thread;}
}
