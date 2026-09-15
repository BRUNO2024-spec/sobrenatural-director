package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NarrativeThreadAffinity {
    private final String threadId; private final int score; private final boolean eligible; private final List<String> reasons;
    public NarrativeThreadAffinity(String threadId,int score,boolean eligible,List<String> reasons){this.threadId=threadId==null?"":threadId;this.score=score;this.eligible=eligible;this.reasons=Collections.unmodifiableList(new ArrayList<String>(reasons==null?Collections.<String>emptyList():reasons));}
    public String getThreadId(){return threadId;} public int getScore(){return score;} public boolean isEligible(){return eligible;} public List<String> getReasons(){return reasons;}
}
