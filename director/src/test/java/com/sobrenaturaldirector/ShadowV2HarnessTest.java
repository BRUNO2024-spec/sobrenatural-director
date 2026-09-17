package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sobrenaturaldirector.config.FoundationConfig;
import com.sobrenaturaldirector.shadow.*;
import org.junit.Test;

/** Synthetic V2 lifecycle harness; never classified as field gameplay. */
public class ShadowV2HarnessTest {
    @Test public void emitsThousandOrderedDecisionsWithLifecycleVariants() throws Exception {
        File root=Files.createTempDirectory("shadow-v2-harness").toFile();
        JsonlShadowV2EventWriter w=new JsonlShadowV2EventWriter(root,ShadowRuntimeIdentity.capture(FoundationConfig.defaults(),ShadowV2HarnessTest.class));
        w.participantStart("ps_HARNESS","p_HARNESS",1);
        for(int i=0;i<1000;i++){
            boolean noAction=i%5==0; String id="d"+i, candidate=noAction?"candidate:no_action":"candidate:action";
            ShadowCandidate c=new ShadowCandidate(candidate,noAction?"NO_ACTION":"ACTION",new double[19],new int[8]);
            ShadowDecisionSnapshot s=new ShadowDecisionSnapshot(id,"DIRECTOR_EXPERIENCE_FEATURES_V4",i+2,"h"+i,candidate,Collections.singletonList(c));
            w.accept(new ShadowScoreResult(s,Collections.singletonMap(candidate,1.0),candidate,10));
            if(!noAction){String execution="e"+i;w.executionStarted(id,execution,i+2);String type=i%5==0?"COMPLETED":i%5==1?"ABORTED":i%5==2?"REPLANNED":i%5==3?"SAFETY_REJECTED":"TIMED_OUT";w.executionTerminal(id,execution,type,i+3,"HARNESS");}
        }
        w.participantEnd("ps_HARNESS","p_HARNESS","DISCONNECT",2002); w.close();
        File session=new File(new File(root,"v2/collection-sessions").listFiles()[0],"events-00000.jsonl");
        Set<Long> sequences=new HashSet<Long>();int decisions=0;long previous=0;
        for(String line:Files.readAllLines(session.toPath())){JsonObject o=new JsonParser().parse(line).getAsJsonObject();long seq=o.get("eventSequence").getAsLong();assertTrue(seq>previous);previous=seq;assertTrue(sequences.add(seq));assertFalse(o.get("trainingAllowed").getAsBoolean());if("DECISION_SNAPSHOT".equals(o.get("eventType").getAsString()))decisions++;}
        assertEquals(1000,decisions);
    }
}
