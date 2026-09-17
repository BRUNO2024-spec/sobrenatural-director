package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import com.sobrenaturaldirector.config.FoundationConfig;
import com.sobrenaturaldirector.shadow.*;
import org.junit.Test;

public class ShadowV2WriterTest {
    @Test public void noActionGetsTerminalAndNeverTouchesV1() throws Exception {
        File root=Files.createTempDirectory("shadow-v2").toFile();
        JsonlShadowV2EventWriter w=new JsonlShadowV2EventWriter(root,ShadowRuntimeIdentity.capture(FoundationConfig.defaults(),ShadowV2WriterTest.class));
        w.participantStart("ps_TEST","p_TEST",1);
        ShadowCandidate c=new ShadowCandidate("candidate:no_action","NO_ACTION",new double[19],new int[8]);
        ShadowDecisionSnapshot s=new ShadowDecisionSnapshot("d","DIRECTOR_EXPERIENCE_FEATURES_V4",2,"fp","candidate:no_action",Collections.singletonList(c));
        w.accept(new ShadowScoreResult(s,Collections.singletonMap("candidate:no_action",1.0),"candidate:no_action",3)); w.participantEnd("ps_TEST","p_TEST","DISCONNECT",3); w.close();
        File v1=new File(root,"shadow-00000.jsonl"); assertFalse(v1.exists());
        File[] sessions=new File(root,"v2/collection-sessions").listFiles(); assertNotNull(sessions); assertEquals(1,sessions.length);
        String text=new String(Files.readAllBytes(new File(sessions[0],"events-00000.jsonl").toPath()),"UTF-8");
        assertTrue(text.contains("DIRECTOR_SHADOW_EXPERIENCE_V2")); assertTrue(text.contains("PARTICIPANT_SESSION_START")); assertTrue(text.contains("DECISION_TERMINAL")); assertTrue(text.contains("\"trainingAllowed\":false"));
    }
    @Test public void diagnosticsAreStructuredAndDoNotChangeChoice() throws Exception {
        File root=Files.createTempDirectory("shadow-v2-diagnostics").toFile(); JsonlShadowV2EventWriter w=new JsonlShadowV2EventWriter(root,ShadowRuntimeIdentity.capture(FoundationConfig.defaults(),ShadowV2WriterTest.class));
        ShadowDecisionDiagnostics d=new ShadowDecisionDiagnostics(3,2,1,0,2,1,1,2,0,2,"NO_ELIGIBLE_ACTION_CANDIDATE",Collections.singleton("MISSING_REQUIRED_CAPABILITY"));
        ShadowCandidate c=new ShadowCandidate("candidate:no_action","NO_ACTION",new double[19],new int[8]); ShadowDecisionSnapshot s=new ShadowDecisionSnapshot("d2","DIRECTOR_EXPERIENCE_FEATURES_V4",2,"fp","candidate:no_action",Collections.singletonList(c),d);
        w.accept(new ShadowScoreResult(s,Collections.singletonMap("candidate:no_action",1.0),"candidate:no_action",1)); w.close(); File session=new File(new File(root,"v2/collection-sessions").listFiles()[0],"events-00000.jsonl"); String text=new String(Files.readAllBytes(session.toPath()),"UTF-8"); assertTrue(text.contains("NO_ELIGIBLE_ACTION_CANDIDATE")); assertTrue(text.contains("\"actionCandidateCount\":2"));
    }
}
