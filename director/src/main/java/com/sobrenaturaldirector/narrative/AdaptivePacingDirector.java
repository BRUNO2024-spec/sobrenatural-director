package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.sobrenaturaldirector.decision.model.DecisionContext;
import com.sobrenaturaldirector.decision.model.Intent;

/** Deterministic, provider-neutral pacing policy. It recommends; it never schedules or executes. */
public final class AdaptivePacingDirector {
    public static final int MAX_HISTORY_EXAMINED=16, MAX_THREADS_CONSIDERED=8, MAX_CANDIDATES_CONSIDERED=16;
    public static final long HIGH_RECOVERY_TICKS=12000L, FULL_RECOVERY_TICKS=24000L;
    public static final double MAX_NO_ACTION_MODIFIER=.25;
    public PacingAssessment assess(String scope, DecisionContext context, List<PacingHistoryEntry> history, Intent candidate, String threadId) {
        if(scope==null||context==null)throw new IllegalArgumentException("pacing scope and context are required");
        List<PacingHistoryEntry> relevant=new ArrayList<PacingHistoryEntry>();for(PacingHistoryEntry e:history==null?Collections.<PacingHistoryEntry>emptyList():history)if(scope.equals(e.getScope())&&e.getTick()<=context.getTick())relevant.add(e);
        Collections.sort(relevant,new Comparator<PacingHistoryEntry>(){public int compare(PacingHistoryEntry a,PacingHistoryEntry b){int c=Long.compare(b.getTick(),a.getTick());return c!=0?c:a.getFingerprint().compareTo(b.getFingerprint());}});if(relevant.size()>MAX_HISTORY_EXAMINED)relevant=new ArrayList<PacingHistoryEntry>(relevant.subList(0,MAX_HISTORY_EXAMINED));
        long latestHigh=-1;int repeated=0;String lastFingerprint="";for(PacingHistoryEntry e:relevant){if(e.getIntensity().getRank()>=NarrativeIntensity.HIGH.getRank()&&latestHigh<0)latestHigh=e.getTick();if(candidate!=null&&e.getIntent()==candidate)repeated++;if(lastFingerprint.length()==0)lastFingerprint=e.getFingerprint();}
        long age=latestHigh<0?Long.MAX_VALUE:context.getTick()-latestHigh;double recovery=age<=HIGH_RECOVERY_TICKS?Math.max(.5,1.0-age/(double)FULL_RECOVERY_TICKS):age==Long.MAX_VALUE?0:Math.max(0,1.0-age/(double)FULL_RECOVERY_TICKS);double allowance=1.0-recovery*.75;double noAction=Math.min(MAX_NO_ACTION_MODIFIER,recovery*.25);List<String> reasons=new ArrayList<String>();PacingMode mode;
        if(recovery>=.75){mode=PacingMode.RECOVERY;reasons.add("RECENT_HIGH_INTENSITY");reasons.add("RECOVERY_NEEDED");}else if(recovery>.25){mode=PacingMode.BUILDING_TENSION;reasons.add("PARTIAL_RECOVERY");}else if(relevant.isEmpty()){mode=PacingMode.QUIET;reasons.add("NO_RECENT_PRESSURE");}else{mode=PacingMode.BALANCED;reasons.add("SUFFICIENT_RECOVERY");}
        if(candidate!=null){NarrativeIntensity i=intensity(candidate);if(i.getRank()>=NarrativeIntensity.HIGH.getRank()&&recovery>.25)reasons.add("HIGH_INTENSITY_DEFERRED");if(repeated>0){reasons.add("REPETITION_PRESSURE");allowance=Math.max(0,allowance-.1);}}
        if(threadId!=null&&!threadId.isEmpty())reasons.add("THREAD_ATTENTION_SCOPED");reasons.add(candidate==Intent.NO_ACTION?"NO_ACTION_PREFERRED":"OPPORTUNITY_EVALUATED");
        return new PacingAssessment(scope,mode,recovery,clamp(allowance),noAction,1.0,reasons,scope+"|"+context.getTick()+"|"+candidate+"|"+lastFingerprint);
    }
    public PacingAssessment assess(String scope,DecisionContext context,List<PacingHistoryEntry> history,Intent candidate){return assess(scope,context,history,candidate,"");}
    public ThreadAttentionDecision assessThreads(DecisionContext context,List<PersistentNarrativeThread> threads,java.util.Set<String> relevantSubjects){if(context==null)throw new IllegalArgumentException("context is required");List<PersistentNarrativeThread> input=new ArrayList<PersistentNarrativeThread>(threads==null?Collections.<PersistentNarrativeThread>emptyList():threads);Collections.sort(input,new Comparator<PersistentNarrativeThread>(){public int compare(PersistentNarrativeThread a,PersistentNarrativeThread b){return a.getId().compareTo(b.getId());}});if(input.size()>MAX_THREADS_CONSIDERED)input=new ArrayList<PersistentNarrativeThread>(input.subList(0,MAX_THREADS_CONSIDERED));List<ThreadAttentionDecision.Candidate> out=new ArrayList<ThreadAttentionDecision.Candidate>();for(PersistentNarrativeThread t:input){if(t.isTerminal())continue;boolean relevant=relevantSubjects!=null&&relevantSubjects.contains(t.getSubject());long waiting=Math.max(0,context.getTick()-t.getLastMeaningfulActivityTick());double score=(relevant?1.0:0.0)+Math.min(.25,waiting/10000.0);List<String> reasons=new ArrayList<String>();reasons.add(relevant?"THREAD_RELEVANCE":"THREAD_LOW_RELEVANCE");if(waiting>0)reasons.add("THREAD_ATTENTION_BALANCE");out.add(new ThreadAttentionDecision.Candidate(t.getId(),score,relevant,reasons));}Collections.sort(out,new Comparator<ThreadAttentionDecision.Candidate>(){public int compare(ThreadAttentionDecision.Candidate a,ThreadAttentionDecision.Candidate b){int c=Double.compare(b.getScore(),a.getScore());return c!=0?c:a.getThreadId().compareTo(b.getThreadId());}});return new ThreadAttentionDecision(out);}
    public static NarrativeIntensity intensity(Intent i){if(i==null||i==Intent.NO_ACTION)return NarrativeIntensity.SUBTLE;switch(i){case AMBIENT_HINT:return NarrativeIntensity.SUBTLE;case LOOT_DISCOVERY:case MYSTERY_SETUP:case ARC_TRANSITION:return NarrativeIntensity.LOW;case STRUCTURE_SEED:case STRUCTURE_REVEAL:case MYSTERY_PAYOFF:case RECOVERY:return NarrativeIntensity.MODERATE;case HORROR_REVEAL:case STALKING_PRESSURE:case MINOR_ENCOUNTER:return NarrativeIntensity.HIGH;case AMBUSH:case BOSS_SETUP:case BOSS_ENCOUNTER:return NarrativeIntensity.EXTREME;default:return NarrativeIntensity.MODERATE;}}
    private static double clamp(double v){return Math.max(0,Math.min(1,v));}
}
