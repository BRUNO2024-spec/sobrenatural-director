package com.sobrenaturaldirector;

import static org.junit.Assert.*;
import java.io.File;
import java.util.Collections;
import com.sobrenaturaldirector.shadow.*;
import org.junit.Test;

public class LearnedScorerModelLoaderTest {
    private static final String MODEL="/home/desktop/Documentos/director-shadow-server/models/learned-scorer-v4-model.json";
    private static final String SHA="dd4ccebad2b7f94db74a27493c2418e7abdea5ee66509d39e2a3982538592af9";
    private static final String CFG="36f2df5d2c9f10afa4f33d2dbf59552b550a567a031b81e25aa20f0173578c76";
    private static final String FEATURES="c6de5c68b7c0744d50acdb012123f229bb796022259f9e17d64bf2fd913cf9d2";
    @Test public void frozenExportLoadsAndScoresFinite() {
        LearnedScorerModelLoader.LoadedModel model=LearnedScorerModelLoader.load(new File(MODEL),SHA,CFG,FEATURES); assertEquals(ShadowModelStatus.READY,model.getStatus());
        ShadowCandidate c=new ShadowCandidate("c","TEST",new double[19],new int[8]); ShadowDecisionSnapshot s=new ShadowDecisionSnapshot("d","DIRECTOR_EXPERIENCE_FEATURES_V4",1,"fp","c",Collections.singletonList(c));
        ShadowScoreResult result=model.getScorer().score(s); assertTrue(Double.isFinite(result.getScores().get("c")));
    }
    @Test public void wrongHashFailsClosed() { assertEquals(ShadowModelStatus.INVALID_HASH,LearnedScorerModelLoader.load(new File(MODEL),zeros(),CFG,FEATURES).getStatus()); }
    private static String zeros(){StringBuilder b=new StringBuilder();for(int i=0;i<64;i++)b.append('0');return b.toString();}
    @Test public void missingModelFailsClosed() { assertEquals(ShadowModelStatus.MISSING,LearnedScorerModelLoader.load(new File("missing-v4.json"),SHA,CFG,FEATURES).getStatus()); }
}
