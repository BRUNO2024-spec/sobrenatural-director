package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.decision.model.Intent;
import com.sobrenaturaldirector.situation.SituationContext;
import com.sobrenaturaldirector.situation.SituationMemory;

/** Immutable, bounded input for intent-to-plan synthesis. */
public final class NarrativePlanSynthesisRequest {
    private final Intent intent; private final SituationContext situation; private final SituationMemory memory;
    private final DirectorProviderRegistry registry; private final NarrativeIntentDecision arbitration;
    private final boolean continuation; private final String narrativeId, regionRelation, synthesisKey, threadId; private final Set<String> tags;
    private final int maxBlueprints, maxFallbacks, maxProviders;
    public NarrativePlanSynthesisRequest(Intent intent, SituationContext situation, SituationMemory memory, DirectorProviderRegistry registry,
            NarrativeIntentDecision arbitration, boolean continuation, String narrativeId, String regionRelation, String synthesisKey,
            Set<String> tags, int maxBlueprints, int maxFallbacks, int maxProviders) {
        this(intent,situation,memory,registry,arbitration,continuation,narrativeId,regionRelation,synthesisKey,tags,maxBlueprints,maxFallbacks,maxProviders,"");
    }
    public NarrativePlanSynthesisRequest(Intent intent, SituationContext situation, SituationMemory memory, DirectorProviderRegistry registry,
            NarrativeIntentDecision arbitration, boolean continuation, String narrativeId, String regionRelation, String synthesisKey,
            Set<String> tags, int maxBlueprints, int maxFallbacks, int maxProviders, String threadId) {
        if (intent == null || situation == null || registry == null || maxBlueprints < 1 || maxFallbacks < 0 || maxProviders < 1) throw new IllegalArgumentException("invalid synthesis request");
        this.intent=intent; this.situation=situation; this.memory=memory == null ? new SituationMemory() : memory; this.registry=registry; this.arbitration=arbitration;
        this.continuation=continuation; this.narrativeId=narrativeId == null ? "" : narrativeId; this.regionRelation=regionRelation == null ? "" : regionRelation; this.synthesisKey=synthesisKey == null ? "" : synthesisKey; this.threadId=threadId == null ? "" : threadId;
        TreeSet<String> copy=new TreeSet<String>(); if(tags!=null) copy.addAll(tags); this.tags=Collections.unmodifiableSet(copy);
        this.maxBlueprints=maxBlueprints; this.maxFallbacks=maxFallbacks; this.maxProviders=maxProviders;
    }
    public Intent getIntent(){return intent;} public SituationContext getSituation(){return situation;} public SituationMemory getMemory(){return memory;}
    public DirectorProviderRegistry getRegistry(){return registry;} public NarrativeIntentDecision getArbitration(){return arbitration;} public boolean isContinuation(){return continuation;}
    public String getNarrativeId(){return narrativeId;} public String getRegionRelation(){return regionRelation;} public String getSynthesisKey(){return synthesisKey;} public String getThreadId(){return threadId;} public Set<String> getTags(){return tags;}
    public int getMaxBlueprints(){return maxBlueprints;} public int getMaxFallbacks(){return maxFallbacks;} public int getMaxProviders(){return maxProviders;}
}
