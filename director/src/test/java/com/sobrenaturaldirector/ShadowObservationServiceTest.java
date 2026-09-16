package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.sobrenaturaldirector.shadow.*;
import org.junit.Test;

public class ShadowObservationServiceTest {
    private ShadowDecisionSnapshot snapshot(String id) { return new ShadowDecisionSnapshot(id,"DIRECTOR_EXPERIENCE_FEATURES_V4",1,"fp","actual",Collections.singletonList(new ShadowCandidate("c","NO_ACTION",new double[19],new int[8]))); }
    @Test public void snapshotIsImmutableAndServiceDoesNotReturnDecision() throws Exception {
        final CountDownLatch done=new CountDownLatch(1);
        ShadowScorer scorer=new ShadowScorer(){public ShadowScoreResult score(ShadowDecisionSnapshot s){return new ShadowScoreResult(s,Collections.singletonMap("c",.5),"c",1);}};
        ShadowEventSink sink=new ShadowEventSink(){public void accept(ShadowScoreResult r){done.countDown();}public void close(){}};
        ShadowObservationService service=new ShadowObservationService(scorer,sink,2);service.start();ShadowDecisionSnapshot s=snapshot("d");service.submit(s);assertTrue(done.await(2,TimeUnit.SECONDS));assertEquals("actual",s.getHeuristicCandidateId());service.stop();
        try{s.getCandidates().add(null);fail("must be immutable");}catch(UnsupportedOperationException expected){}
    }
    @Test public void queueOverflowDropsWithoutBlocking() throws Exception {
        final CountDownLatch scoring=new CountDownLatch(1); final CountDownLatch release=new CountDownLatch(1);
        ShadowScorer scorer=new ShadowScorer(){public ShadowScoreResult score(ShadowDecisionSnapshot s){scoring.countDown();try{release.await(2,TimeUnit.SECONDS);}catch(InterruptedException e){}return new ShadowScoreResult(s,Collections.singletonMap("c",.5),"c",1);}};
        ShadowObservationService service=new ShadowObservationService(scorer,new ShadowEventSink(){public void accept(ShadowScoreResult r){}public void close(){}},1);service.start();service.submit(snapshot("a"));assertTrue(scoring.await(2,TimeUnit.SECONDS));service.submit(snapshot("b"));long start=System.nanoTime();service.submit(snapshot("c"));long elapsed=System.nanoTime()-start;assertTrue(elapsed<100000000L);assertTrue(service.getMetrics().getDropped()>=1);release.countDown();service.stop();
    }
    @Test public void scorerFailureIsContained() throws Exception {
        final CountDownLatch done=new CountDownLatch(1);ShadowObservationService service=new ShadowObservationService(new ShadowScorer(){public ShadowScoreResult score(ShadowDecisionSnapshot s){throw new RuntimeException("synthetic failure");}},new ShadowEventSink(){public void accept(ShadowScoreResult r){done.countDown();}public void close(){}},2);service.start();service.submit(snapshot("x"));Thread.sleep(100);assertEquals(1,service.getMetrics().getErrors());assertEquals(0,service.getMetrics().getScored());service.stop();
    }
    @Test public void outcomesExpireAndNeverBecomeTrainingLabels() {
        ShadowOutcomeTracker tracker=new ShadowOutcomeTracker(2,10);assertTrue(tracker.open("d",1));assertEquals(1,tracker.size());assertEquals(0,tracker.expire(10));assertEquals(1,tracker.expire(11));assertEquals(0,tracker.size());
        ShadowDecisionSnapshot s=snapshot("d2");ShadowScoreResult r=new ShadowScoreResult(s,Collections.singletonMap("c",.5),"c",1);ShadowExperienceRecord e=new ShadowExperienceRecord(s,r,new ShadowOutcome("d2",ShadowOutcome.Status.COMPLETE,2,0,"done"));assertFalse(e.isTrainingAllowed());
    }
    @Test public void disabledServiceDoesNotQueueOrRun() { ShadowObservationService service=new ShadowObservationService(new ShadowScorer(){public ShadowScoreResult score(ShadowDecisionSnapshot s){fail();return null;}},new ShadowEventSink(){public void accept(ShadowScoreResult r){}public void close(){}},1);assertFalse(service.isEnabled());assertFalse(service.submit(snapshot("disabled")));assertEquals(0,service.getMetrics().getSubmitted()); }
}
