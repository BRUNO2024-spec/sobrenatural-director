package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import com.sobrenaturaldirector.shadow.*;
import org.junit.Test;

/** Synthetic E2E closure: snapshot -> frozen V4 inference -> JSONL -> outcome. */
public class ShadowPipelineHarnessTest {
    private static final String MODEL="/home/desktop/Documentos/director-shadow-server/models/learned-scorer-v4-model.json";
    @Test public void generatesOneThousandJoinableExperiences() throws Exception {
        LearnedScorerModelLoader.LoadedModel m=LearnedScorerModelLoader.load(new File(MODEL),"dd4ccebad2b7f94db74a27493c2418e7abdea5ee66509d39e2a3982538592af9","36f2df5d2c9f10afa4f33d2dbf59552b550a567a031b81e25aa20f0173578c76","c6de5c68b7c0744d50acdb012123f229bb796022259f9e17d64bf2fd913cf9d2"); assertEquals(ShadowModelStatus.READY,m.getStatus());
        File dir=Files.createTempDirectory("shadow-pipeline").toFile(); JsonlShadowEventWriter w=new JsonlShadowEventWriter(dir,1024*1024,32*1024*1024); ShadowOutcomeTracker outcomes=new ShadowOutcomeTracker(32,100);
        for(int i=0;i<1000;i++){String id="harness-"+i; ShadowDecisionSnapshot s=new ShadowDecisionSnapshot(id,"DIRECTOR_EXPERIENCE_FEATURES_V4",i,"fp-"+i,"a",Arrays.asList(new ShadowCandidate("a","TEST",new double[19],new int[8]),new ShadowCandidate("b","TEST",new double[19],new int[8]))); assertTrue(outcomes.open(id,i)); w.accept(m.getScorer().score(s)); assertNotNull(outcomes.close(id,ShadowOutcome.Status.COMPLETE,i+1,0,"harness"));} w.close();
        File[] segments=dir.listFiles(); assertNotNull(segments); int lines=0; for(File f:segments)for(String line:Files.readAllLines(f.toPath()))if(line.trim().length()>0)lines++; assertEquals(3000,lines);
    }
}
