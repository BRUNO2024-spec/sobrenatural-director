package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import com.sobrenaturaldirector.shadow.*;
import org.junit.Test;

public class ShadowJsonlWriterTest {
    @Test public void writerEmitsJoinableDecisionAndActualRecords() throws Exception {
        File dir=Files.createTempDirectory("shadow-jsonl").toFile(); ShadowCandidate c=new ShadowCandidate("c","TEST",new double[19],new int[8]);
        ShadowDecisionSnapshot s=new ShadowDecisionSnapshot("d","DIRECTOR_EXPERIENCE_FEATURES_V4",1,"fp","c",Collections.singletonList(c));
        JsonlShadowEventWriter writer=new JsonlShadowEventWriter(dir,4096,8192); writer.accept(new ShadowScoreResult(s,Collections.singletonMap("c",1.0),"c",2)); writer.close();
        File[] files=dir.listFiles(); assertNotNull(files); assertEquals(1,files.length); String text=new String(Files.readAllBytes(files[0].toPath()),"UTF-8"); assertTrue(text.contains("DECISION_SNAPSHOT")); assertTrue(text.contains("ACTUAL_DECISION")); assertTrue(text.contains("SHADOW_SCORE_RESULT")); assertEquals(3,text.trim().split("\\n").length); assertTrue(text.contains("\"decisionId\":\"d\""));
    }
}
