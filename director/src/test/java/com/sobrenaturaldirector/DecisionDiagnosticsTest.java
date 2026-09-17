package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.sobrenaturaldirector.decision.DecisionEngine;
import com.sobrenaturaldirector.decision.model.*;
import com.sobrenaturaldirector.shadow.*;
import org.junit.Test;

public class DecisionDiagnosticsTest {
    private ShadowDecisionSnapshot observe(DecisionContext context) throws Exception { return observe(context,null); }
    private ShadowDecisionSnapshot observe(DecisionContext context,List<CandidateAction> candidates) throws Exception {
        final CountDownLatch done=new CountDownLatch(1); final AtomicReference<ShadowDecisionSnapshot> value=new AtomicReference<ShadowDecisionSnapshot>();
        ShadowObservationService service=new ShadowObservationService(new ShadowScorer(){public ShadowScoreResult score(ShadowDecisionSnapshot s){value.set(s);done.countDown();return new ShadowScoreResult(s,Collections.singletonMap(s.getCandidates().get(0).getId(),1.0),s.getCandidates().get(0).getId(),1);}},new ShadowEventSink(){public void accept(ShadowScoreResult r){}public void close(){}},2);
        service.start(); if(candidates==null)new DecisionEngine(service).decide(context,1);else new DecisionEngine(service).decide(context,candidates,1); assertTrue(done.await(2,TimeUnit.SECONDS)); service.stop(); return value.get();
    }
    @Test public void calmContextReportsOnlyNoActionCandidate() throws Exception {CandidateAction only=new CandidateAction("candidate:no_action",Intent.NO_ACTION,"TEST",.5,null,null,null,ActionSafety.NON_DESTRUCTIVE);ShadowDecisionSnapshot s=observe(DecisionContext.calm(1),Collections.singletonList(only));assertNotNull(s.getDiagnostics());assertEquals(0,s.getDiagnostics().getActionCandidateCount());assertEquals("NO_ACTION_ONLY_CANDIDATE",s.getDiagnostics().getPrimaryReason());}
    @Test public void activeContextReportsActionCandidatesSeparately() throws Exception {Map<String,Double> b=Collections.singletonMap("threat",10.0);DecisionContext c=new DecisionContext(10,10,.9,.9,.1,.1,1,.8,.1,true,false,true,1,b,Collections.singletonMap("HORROR",com.sobrenaturaldirector.decision.model.ProviderStatus.AVAILABLE),Collections.singleton("COMMON_THREAT"),Collections.<String>emptySet(),Collections.<String>emptySet(),Collections.<String,Long>emptyMap());ShadowDecisionDiagnostics d=observe(c).getDiagnostics();assertTrue(d.getActionCandidateCount()>0);assertNotEquals("NO_ACTION_ONLY_CANDIDATE",d.getPrimaryReason());}
}
