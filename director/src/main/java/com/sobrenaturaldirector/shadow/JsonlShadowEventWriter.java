package com.sobrenaturaldirector.shadow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/** Local bounded JSONL sink. It records no player identity or network data. */
public final class JsonlShadowEventWriter implements ShadowEventSink {
    private final File root; private final long segmentLimit,totalLimit; private long total,segment; private int index; private BufferedWriter writer;
    public JsonlShadowEventWriter(File root,long segmentLimit,long totalLimit){if(root==null||segmentLimit<1||totalLimit<segmentLimit)throw new IllegalArgumentException("invalid shadow disk budget");this.root=root;this.segmentLimit=segmentLimit;this.totalLimit=totalLimit;}
    public synchronized void accept(ShadowScoreResult r){String line=serialize(r);String actual=actual(r);String snapshot=snapshot(r);long bytes=(line+actual+snapshot).getBytes(java.nio.charset.Charset.forName("UTF-8")).length;if(total+bytes>totalLimit)throw new IllegalStateException("shadow disk budget reached");try{if(writer==null||segment+bytes>segmentLimit)rotate();writer.write(snapshot);writer.newLine();writer.write(actual);writer.newLine();writer.write(line);writer.newLine();writer.flush();segment+=bytes;total+=bytes;}catch(IOException e){throw new IllegalStateException("shadow writer failure",e);}}
    private void rotate()throws IOException{if(writer!=null)writer.close();if(!root.exists()&&!root.mkdirs())throw new IOException("cannot create shadow directory");writer=new BufferedWriter(new FileWriter(new File(root,String.format(Locale.ROOT,"shadow-%05d.jsonl",index++))));segment=0;}
    private String serialize(ShadowScoreResult r){StringBuilder b=new StringBuilder();b.append("{\"eventType\":\"SHADOW_SCORE_RESULT\",\"schema\":\"DIRECTOR_SHADOW_EXPERIENCE_V1\",\"featureSchema\":\"").append(esc(r.getSnapshot().getSchema())).append("\",\"decisionId\":\"").append(esc(r.getSnapshot().getDecisionId())).append("\",\"tick\":").append(r.getSnapshot().getTick()).append(",\"heuristicCandidateId\":\"").append(esc(r.getSnapshot().getHeuristicCandidateId())).append("\",\"shadowSelectedId\":\"").append(esc(r.getShadowSelectedId())).append("\",\"latencyNanos\":").append(r.getLatencyNanos()).append(",\"scores\":{");boolean first=true;for(java.util.Map.Entry<String,Double> e:r.getScores().entrySet()){if(!first)b.append(',');first=false;b.append('\"').append(esc(e.getKey())).append("\":").append(e.getValue());}return b.append("}}").toString();}
    private String snapshot(ShadowScoreResult r){return "{\"eventType\":\"DECISION_SNAPSHOT\",\"schema\":\"DIRECTOR_SHADOW_EXPERIENCE_V1\",\"featureSchema\":\""+esc(r.getSnapshot().getSchema())+"\",\"decisionId\":\""+esc(r.getSnapshot().getDecisionId())+"\",\"tick\":"+r.getSnapshot().getTick()+",\"candidateCount\":"+r.getSnapshot().getCandidates().size()+",\"trainingAllowed\":false}";}
    private String actual(ShadowScoreResult r){return "{\"eventType\":\"ACTUAL_DECISION\",\"schema\":\"DIRECTOR_SHADOW_EXPERIENCE_V1\",\"decisionId\":\""+esc(r.getSnapshot().getDecisionId())+"\",\"actualCandidateId\":\""+esc(r.getSnapshot().getHeuristicCandidateId())+"\",\"tick\":"+r.getSnapshot().getTick()+",\"trainingAllowed\":false}";}
    private static String esc(String x){return x.replace("\\","\\\\").replace("\"","\\\"");}
    public synchronized void close(){try{if(writer!=null)writer.close();}catch(IOException ignored){}}
}
