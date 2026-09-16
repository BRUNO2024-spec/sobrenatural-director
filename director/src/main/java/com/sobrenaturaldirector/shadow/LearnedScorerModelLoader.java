package com.sobrenaturaldirector.shadow;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Strict loader for the dependency-free frozen V4 JSON export. */
public final class LearnedScorerModelLoader {
    public static final String SCHEMA="DIRECTOR_EXPERIENCE_FEATURES_V4";
    private static final int FORMAT=1, NUMERIC=19, CATEGORICAL=8, EMBEDDING=4;
    private LearnedScorerModelLoader() { }
    public static LoadedModel load(File file,String expectedSha,String expectedConfigSha,String expectedFeatureSha) {
        if(file==null) return fail(ShadowModelStatus.NOT_CONFIGURED);
        if(!file.isFile()) return fail(ShadowModelStatus.MISSING);
        try {
            byte[] bytes=read(file); if(expectedSha==null||expectedSha.length()!=64||!expectedSha.equals(sha(bytes)))return fail(ShadowModelStatus.INVALID_HASH);
            JsonObject root=new JsonParser().parse(new String(bytes,Charset.forName("UTF-8"))).getAsJsonObject();
            if(!root.has("formatVersion")||root.get("formatVersion").getAsInt()!=FORMAT)return fail(ShadowModelStatus.INVALID_FORMAT);
            if(!expectedConfigSha.equals(root.get("frozenConfigSha256").getAsString()))return fail(ShadowModelStatus.INVALID_CONFIG);
            if(!expectedFeatureSha.equals(root.get("featureSchemaSha256").getAsString()))return fail(ShadowModelStatus.INVALID_SCHEMA);
            if(!arrayEquals(root.getAsJsonArray("numericFeatureOrder"),numericNames())||root.getAsJsonArray("categoricalFeatureOrder").size()!=CATEGORICAL)return fail(ShadowModelStatus.INVALID_SCHEMA);
            if(root.get("embeddingDim").getAsInt()!=EMBEDDING||root.get("oovIndex").getAsInt()!=0)return fail(ShadowModelStatus.INVALID_SCHEMA);
            double[][][] embeddings=embeddings(root.getAsJsonArray("embeddings")); double[] means=normalization(root.getAsJsonObject("normalization").getAsJsonObject("means"),numericNames()); double[] stds=normalization(root.getAsJsonObject("normalization").getAsJsonObject("stds"),numericNames());
            List<double[][]> weights=new ArrayList<double[][]>(); List<double[]> biases=new ArrayList<double[]>(); JsonArray layers=root.getAsJsonArray("layers");
            for(JsonElement element:layers){JsonObject layer=element.getAsJsonObject();String type=layer.get("type").getAsString();if("relu".equals(type))continue;if(!"linear".equals(type))return fail(ShadowModelStatus.INVALID_FORMAT);weights.add(matrix(layer.getAsJsonArray("weight")));biases.add(vector(layer.getAsJsonArray("bias")));}
            if(weights.size()!=3||weights.get(0).length!=128||weights.get(0)[0].length!=51||weights.get(1).length!=64||weights.get(1)[0].length!=128||weights.get(2).length!=1||weights.get(2)[0].length!=64)return fail(ShadowModelStatus.INVALID_FORMAT);
            return new LoadedModel(new FrozenV4Scorer(embeddings,means,stds,weights,biases));
        } catch (RuntimeException e) { return fail(ShadowModelStatus.INVALID_FORMAT); } catch (Exception e) { return fail(ShadowModelStatus.RUNTIME_ERROR); }
    }
    private static LoadedModel fail(ShadowModelStatus s){return new LoadedModel(s);}
    private static byte[] read(File f)throws Exception{FileInputStream in=new FileInputStream(f);java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))>=0)out.write(b,0,n);in.close();return out.toByteArray();}
    private static String sha(byte[] b)throws Exception{byte[] d=MessageDigest.getInstance("SHA-256").digest(b);StringBuilder s=new StringBuilder();for(byte x:d)s.append(String.format("%02x",x&255));return s.toString();}
    private static String[] numericNames(){return new String[]{"tension","pressure","fatigue","recoveryNeed","healthRatio","combatPower","isolation","underground","observingSite","safetyKnown","eventConcurrency","memoryPressure","cooldownActive","threadActive","threadAgeBucket","recentHigh","repetitionCount","episodeStep","baseUtility"};}
    private static boolean arrayEquals(JsonArray a,String[] x){if(a==null||a.size()!=x.length)return false;for(int i=0;i<x.length;i++)if(!x[i].equals(a.get(i).getAsString()))return false;return true;}
    private static double[] normalization(JsonObject o,String[] names){double[] x=new double[names.length];for(int i=0;i<x.length;i++){x[i]=o.get(names[i]).getAsDouble();finite(x[i]);}return x;}
    private static double[][][] embeddings(JsonArray a){if(a==null||a.size()!=CATEGORICAL)throw new IllegalArgumentException();double[][][] out=new double[CATEGORICAL][][];for(int i=0;i<a.size();i++){JsonArray rows=a.get(i).getAsJsonArray();out[i]=new double[rows.size()][EMBEDDING];for(int j=0;j<rows.size();j++){JsonArray row=rows.get(j).getAsJsonArray();if(row.size()!=EMBEDDING)throw new IllegalArgumentException();for(int k=0;k<EMBEDDING;k++){out[i][j][k]=row.get(k).getAsDouble();finite(out[i][j][k]);}}}return out;}
    private static double[][] matrix(JsonArray a){double[][] x=new double[a.size()][];for(int i=0;i<x.length;i++){JsonArray r=a.get(i).getAsJsonArray();x[i]=new double[r.size()];for(int j=0;j<r.size();j++){x[i][j]=r.get(j).getAsDouble();finite(x[i][j]);}}return x;}
    private static double[] vector(JsonArray a){double[] x=new double[a.size()];for(int i=0;i<x.length;i++){x[i]=a.get(i).getAsDouble();finite(x[i]);}return x;}
    private static void finite(double x){if(Double.isNaN(x)||Double.isInfinite(x))throw new IllegalArgumentException("nonfinite model weight");}
    public static final class LoadedModel {private final ShadowModelStatus status;private final ShadowScorer scorer;private LoadedModel(ShadowModelStatus s){status=s;scorer=null;}private LoadedModel(ShadowScorer s){status=ShadowModelStatus.READY;scorer=s;}public ShadowModelStatus getStatus(){return status;}public ShadowScorer getScorer(){if(scorer==null)throw new IllegalStateException("model not ready");return scorer;}}
    private static final class FrozenV4Scorer implements ShadowScorer {private final double[][][] e;private final double[] mean,std;private final List<double[][]> w;private final List<double[]> b;FrozenV4Scorer(double[][][]e,double[]m,double[]s,List<double[][]>w,List<double[]>b){this.e=e;mean=m;std=s;this.w=w;this.b=b;}
        public ShadowScoreResult score(ShadowDecisionSnapshot snapshot){long start=System.nanoTime();java.util.Map<String,Double> scores=new java.util.LinkedHashMap<String,Double>();String selected=null;double best=-Double.MAX_VALUE;for(ShadowCandidate c:snapshot.getCandidates()){double[] x=new double[51];double[] n=c.getNumeric();if(n.length!=19)throw new IllegalArgumentException("numeric feature count");for(int i=0;i<19;i++)x[i]=(n[i]-mean[i])/std[i];int at=19;int[] cats=c.getCategorical();if(cats.length!=8)throw new IllegalArgumentException("categorical feature count");for(int i=0;i<8;i++){int id=cats[i]<e[i].length?cats[i]:0;for(int j=0;j<4;j++)x[at++]=e[i][id][j];}for(int layer=0;layer<3;layer++){double[] z=new double[w.get(layer).length];for(int i=0;i<z.length;i++){double sum=b.get(layer)[i];for(int j=0;j<x.length;j++)sum+=w.get(layer)[i][j]*x[j];z[i]=sum;}if(layer<2)for(int i=0;i<z.length;i++)z[i]=Math.max(0,z[i]);x=z;}double value=x[0];finite(value);scores.put(c.getId(),value);if(value>best){best=value;selected=c.getId();}}return new ShadowScoreResult(snapshot,scores,selected,System.nanoTime()-start);}
    }
}
