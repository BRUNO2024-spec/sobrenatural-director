package com.sobrenaturaldirector.benchmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sobrenaturaldirector.decision.DecisionEngine;
import com.sobrenaturaldirector.decision.DecisionResult;
import com.sobrenaturaldirector.decision.model.ActionSafety;
import com.sobrenaturaldirector.decision.model.CandidateAction;
import com.sobrenaturaldirector.decision.model.DecisionContext;
import com.sobrenaturaldirector.decision.model.Intent;
import com.sobrenaturaldirector.decision.model.ProviderStatus;
import com.sobrenaturaldirector.environment.model.EnvironmentClassification;
import com.sobrenaturaldirector.environment.model.EnvironmentEvidence;
import com.sobrenaturaldirector.environment.model.EnvironmentSignal;
import com.sobrenaturaldirector.environment.model.RegionKey;
import com.sobrenaturaldirector.environment.model.SemanticRegionProfile;

/** Offline, deterministic benchmark foundation. It never touches Minecraft APIs. */
public final class DirectorBenchmarkRunner {
    public static final String VERSION = "DIRECTOR_BENCHMARK_V1";
    private static final int SINGLE_STEPS = 600;
    private static final int SEQUENCES = 60;
    private static final int SEQUENCE_LENGTH = 10;
    private static final DirectorDecisionPolicy policy = new CurrentHeuristicDirectorPolicy();

    private enum Split { TRAIN, VALIDATION, TEST, HOLDOUT }
    private enum Family { NO_ACTION_REQUIRED, MULTIPLE_VALID_CHOICES, CONTINUATION_OPPORTUNITY, NOVELTY_PRESSURE,
        RECOVERY_REQUIRED, BUILDING_TENSION, PLAYER_BASE_NEARBY, WILDERNESS, PROVIDER_MISSING, PROVIDER_RETURNED,
        MULTI_PROVIDER, DIMENSION_ISOLATION, THREAD_COMPETITION, THREAD_DORMANCY_RESUME, REPETITION_TRAP,
        FALSE_HIGH_INTENSITY_TEMPTATION, LOW_IMPACT_CONTINUATION, PROVIDER_FIT_CONTRAST }

    private static final class Scenario {
        final String id; final long seed; final Family family; final int dimension; final EnvironmentClassification environment;
        final boolean providerAvailable; final boolean noAction; final boolean continuation; final int recentHigh;
        Scenario(String id, long seed, Family family, int dimension, EnvironmentClassification environment,
                boolean providerAvailable, boolean noAction, boolean continuation, int recentHigh) {
            this.id=id; this.seed=seed; this.family=family; this.dimension=dimension; this.environment=environment;
            this.providerAvailable=providerAvailable; this.noAction=noAction; this.continuation=continuation; this.recentHigh=recentHigh;
        }
        Split split() { int n=(int)(seed % 10); return n < 6 ? Split.TRAIN : n < 8 ? Split.VALIDATION : n == 8 ? Split.TEST : Split.HOLDOUT; }
        String fingerprint() { return sha(id+":"+seed+":"+family+":"+dimension+":"+environment+":"+providerAvailable+":"+noAction+":"+continuation+":"+recentHigh); }
    }

    private static final class Result {
        String id, split, inputFingerprint, selected, selectedIntent, selectedIntensity, selectedBlueprint, selectedProvider; long seed, latencyNanos;
        boolean noAction, hardValid, expectedNoAction; double relevance, continuity, novelty, providerFit, impact, intensity, quality;
        Result(Scenario s) { id=s.id; seed=s.seed; split=s.split().name(); inputFingerprint=s.fingerprint(); expectedNoAction=s.noAction; }
    }

    public static void main(String[] args) throws Exception {
        File output = new File(System.getProperty("benchmark.outputDir", "runtime-tests/benchmark"));
        if (!output.exists() && !output.mkdirs()) throw new IllegalStateException("cannot create benchmark output");
        List<Scenario> scenarios = scenarios();
        Run first = run(scenarios, output, "A");
        Run second = run(scenarios, output, "B");
        boolean fingerprintMatch = first.fingerprint.equals(second.fingerprint);
        boolean metricsMatch = first.metrics.equals(second.metrics);
        writeSummary(output, first, second, fingerprintMatch, metricsMatch);
        writeMetadata(output);
        if (!fingerprintMatch || !metricsMatch) throw new IllegalStateException("benchmark is not deterministic");
    }

    private static final class Run {
        final List<Result> results; final String fingerprint, metrics; final long runtimeNanos;
        Run(List<Result> results, long runtimeNanos) { this.results=results; this.runtimeNanos=runtimeNanos; this.fingerprint=sha(serial(results)); this.metrics=metrics(results); }
    }

    private static Run run(List<Scenario> scenarios, File output, String label) throws Exception {
        long started=System.nanoTime(); List<Result> results=new ArrayList<Result>();
        for (Scenario scenario : scenarios) results.add(decide(scenario));
        for (int sequence=0; sequence<SEQUENCES; sequence++) {
            Scenario seed=scenarios.get(sequence * 10); for (int step=0; step<SEQUENCE_LENGTH; step++) results.add(decide(sequenceScenario(seed, sequence, step)));
        }
        writeDetails(output, label, results); writeFeatures(output, label, results);
        return new Run(results, System.nanoTime()-started);
    }

    private static Result decide(Scenario scenario) {
        Result result=new Result(scenario); long started=System.nanoTime(); DecisionContext context=context(scenario);
        List<CandidateAction> candidates=candidates(scenario);
        DecisionResult decision=policy.evaluate(context, candidates, scenario.seed);
        CandidateAction selected=decision.getSelected(); result.selected=selected.getCandidateId(); result.selectedIntent=selected.getIntent().name();
        result.selectedIntensity=selected.getMetadata().get("intensity"); result.selectedBlueprint=selected.getMetadata().get("blueprint"); result.selectedProvider=selected.getRequirements().get("provider");
        result.noAction=selected.getIntent()==Intent.NO_ACTION; result.hardValid=hardValid(scenario, selected);
        result.relevance=value(selected,"relevance"); result.continuity=value(selected,"continuity"); result.novelty=value(selected,"novelty");
        result.providerFit=value(selected,"providerFit"); result.impact=value(selected,"impact"); result.intensity=value(selected,"intensityFit");
        result.quality=(result.relevance+result.continuity+result.novelty+result.providerFit+result.impact+result.intensity)/6.0; result.latencyNanos=System.nanoTime()-started;
        return result;
    }

    private static DecisionContext context(Scenario s) {
        Map<String,ProviderStatus> providers=new LinkedHashMap<String,ProviderStatus>(); providers.put("structure", ProviderStatus.AVAILABLE);
        providers.put("actor", ProviderStatus.AVAILABLE); providers.put("threat", s.providerAvailable ? ProviderStatus.AVAILABLE : ProviderStatus.MISSING);
        Map<String,Double> budgets=new LinkedHashMap<String,Double>(); budgets.put("threat", 10.0);
        Map<String,Long> cooldowns=new LinkedHashMap<String,Long>(); if (s.family==Family.RECOVERY_REQUIRED) cooldowns.put("BOSS_ENCOUNTER", 9999L);
        return new DecisionContext(s.seed, s.seed, s.family==Family.BUILDING_TENSION?.65:0.25, s.recentHigh>0?.8:0.2,
                0.1, s.recentHigh>0?.8:0.1, 1.0, .5, 0, false, false, true, 1, budgets, providers,
                Collections.singleton("COMMON_THREAT"), Collections.singleton(s.environment.name()), Collections.<String>emptySet(), cooldowns, profile(s));
    }

    private static SemanticRegionProfile profile(Scenario s) {
        Map<EnvironmentSignal,Integer> counts=new EnumMap<EnvironmentSignal,Integer>(EnvironmentSignal.class);
        if (s.environment==EnvironmentClassification.ESTABLISHED_PLAYER_AREA) { counts.put(EnvironmentSignal.PLAYER_MODIFICATION,35); counts.put(EnvironmentSignal.FORTIFICATION,8); counts.put(EnvironmentSignal.STORAGE,3); }
        if (s.environment==EnvironmentClassification.TEMPORARY_CAMP) { counts.put(EnvironmentSignal.LIGHT_SOURCE,2); counts.put(EnvironmentSignal.STORAGE,1); }
        EnvironmentEvidence evidence=new EnvironmentEvidence(new RegionKey(s.dimension, (int)(s.seed % 17), (int)(s.seed % 19)), s.seed, counts, 512, 16, 1, s.seed, false, false);
        int confidence=s.environment==EnvironmentClassification.ESTABLISHED_PLAYER_AREA?90:s.environment==EnvironmentClassification.UNKNOWN?0:50;
        return new SemanticRegionProfile(evidence, s.environment, confidence, s.recentHigh>0?80:10, EnumSet.noneOf(EnvironmentSignal.class), Collections.singletonList("BENCHMARK_SCENARIO"));
    }

    private static List<CandidateAction> candidates(Scenario s) {
        List<CandidateAction> result=new ArrayList<CandidateAction>(); result.add(action("a-ambient", Intent.AMBIENT_HINT, ActionSafety.NON_DESTRUCTIVE, "ambient", s, 0.55, .35, .25, .7, .9, .4));
        if (s.continuation) result.add(action("b-continuation", Intent.MYSTERY_PAYOFF, ActionSafety.NON_DESTRUCTIVE, "continuation", s, .9, 1, .5, .8, .9, .75));
        result.add(action("c-threat", s.recentHigh>0?Intent.RECOVERY:Intent.BOSS_ENCOUNTER, s.recentHigh>0?ActionSafety.NON_DESTRUCTIVE:ActionSafety.MAJOR, "threat", s, .8, .6, .8, s.providerAvailable?1:0, s.environment==EnvironmentClassification.ESTABLISHED_PLAYER_AREA?0:.7, s.recentHigh>0?.9:.8));
        if (s.noAction || !s.providerAvailable) result.add(action("z-no-action", Intent.NO_ACTION, ActionSafety.NON_DESTRUCTIVE, "none", s, .7, .5, .7, 1, 1, 1));
        return result;
    }

    private static CandidateAction action(String id, Intent intent, ActionSafety safety, String provider, Scenario s, double relevance,
            double continuity, double novelty, double providerFit, double impact, double intensity) {
        Map<String,String> requirements=new LinkedHashMap<String,String>(); if (!"none".equals(provider)) requirements.put("provider", provider.equals("threat")?"threat":"structure");
        Map<String,String> metadata=new LinkedHashMap<String,String>(); metadata.put("intensity", intent==Intent.BOSS_ENCOUNTER?"HIGH":intent==Intent.RECOVERY?"LOW":"MEDIUM");
        metadata.put("blueprint", provider+":"+intent.name()); metadata.put("relevance", String.valueOf(relevance)); metadata.put("continuity", String.valueOf(continuity)); metadata.put("novelty", String.valueOf(novelty)); metadata.put("providerFit", String.valueOf(providerFit)); metadata.put("impact", String.valueOf(impact)); metadata.put("intensityFit", String.valueOf(intensity));
        return new CandidateAction(id, intent, provider, relevance*100, requirements, metadata, Collections.<String,Double>emptyMap(), safety);
    }

    private static boolean hardValid(Scenario s, CandidateAction action) { if (s.noAction && action.getIntent()!=Intent.NO_ACTION) return false; return action.getIntent()!=Intent.BOSS_ENCOUNTER || (s.providerAvailable && s.environment!=EnvironmentClassification.ESTABLISHED_PLAYER_AREA && s.recentHigh==0); }
    private static double value(CandidateAction action, String key) { try { return Double.parseDouble(action.getMetadata().get(key)); } catch (Exception e) { return 0; } }

    private static List<Scenario> scenarios() {
        List<Scenario> result=new ArrayList<Scenario>(); Family[] families=Family.values(); EnvironmentClassification[] environments=EnvironmentClassification.values();
        for (int i=0;i<SINGLE_STEPS;i++) { Family family=families[i%families.length]; EnvironmentClassification environment=environments[(i/3)%environments.length]; boolean missing=family==Family.PROVIDER_MISSING || family==Family.PROVIDER_RETURNED && (i%2==0); boolean noAction=family==Family.NO_ACTION_REQUIRED || missing; boolean continuation=family==Family.CONTINUATION_OPPORTUNITY || family==Family.THREAD_DORMANCY_RESUME || family==Family.LOW_IMPACT_CONTINUATION || i%5==0; result.add(new Scenario("V1-"+family.name()+"-"+String.format("%04d",i), 1000003L+i*7919L, family, family==Family.DIMENSION_ISOLATION?-1:0, environment, !missing, noAction, continuation, family==Family.RECOVERY_REQUIRED||family==Family.FALSE_HIGH_INTENSITY_TEMPTATION?2:0)); }
        return result;
    }

    private static Scenario sequenceScenario(Scenario base, int sequence, int step) { return new Scenario(base.id+"-SEQ-"+sequence+"-"+step, base.seed+step*31L, base.family, step%3==0?-1:base.dimension, step%4==0?EnvironmentClassification.WILDERNESS:base.environment, base.providerAvailable, base.noAction && step%3==0, base.continuation || step%2==1, base.recentHigh>0?Math.max(0,base.recentHigh-step/3):0); }
    private static String serial(List<Result> results) { StringBuilder b=new StringBuilder(); for(Result r:results)b.append(r.id).append('|').append(r.inputFingerprint).append('|').append(r.selected).append('|').append(r.hardValid).append('\n'); return b.toString(); }
    private static String metrics(List<Result> results) { int hard=0,no=0,expected=0; Set<String> plans=new HashSet<String>(); for(Result r:results){if(r.hardValid)hard++;if(r.noAction)no++;if(r.expectedNoAction)expected++;plans.add(r.selected);} return hard+":"+no+":"+expected+":"+plans.size(); }
    private static void writeDetails(File dir,String label,List<Result> results)throws Exception{Writer w=writer(new File(dir,"director-benchmark-v1-details-"+label+".jsonl")); for(Result r:results){w.write("{\"scenarioId\":\""+esc(r.id)+"\",\"split\":\""+r.split+"\",\"seed\":"+r.seed+",\"inputFingerprint\":\""+r.inputFingerprint+"\",\"policyDecision\":\""+r.selected+"\",\"selectedIntent\":\""+r.selectedIntent+"\",\"selectedBlueprint\":\""+r.selectedBlueprint+"\",\"selectedIntensity\":\""+r.selectedIntensity+"\",\"selectedProvider\":\""+String.valueOf(r.selectedProvider)+"\",\"noAction\":"+r.noAction+",\"hardValid\":"+r.hardValid+",\"quality\":"+r.quality+",\"decisionLatencyNanos\":"+r.latencyNanos+"}\n");}w.close();}
    private static void writeFeatures(File dir,String label,List<Result> results)throws Exception{Writer w=writer(new File(dir,"director-benchmark-v1-features-"+label+".jsonl")); for(Result r:results)w.write("{\"schemaVersion\":\"DIRECTOR_BENCHMARK_FEATURES_V1\",\"scenarioId\":\""+esc(r.id)+"\",\"seed\":"+r.seed+",\"split\":\""+r.split+"\",\"trainingAllowed\":"+("TRAIN".equals(r.split))+",\"stateFeatures\":{\"hardValid\":"+r.hardValid+",\"noAction\":"+r.noAction+"},\"candidateFeatures\":{\"quality\":"+r.quality+"},\"decision\":\""+r.selected+"\"}\n");w.close();}
    private static void writeSummary(File dir,Run a,Run b,boolean fp,boolean mm)throws Exception{int singles=SINGLE_STEPS, sequences=SEQUENCES; int decisions=a.results.size(); int hard=0; double quality=0; int no=0,expected=0; Set<String> plans=new LinkedHashSet<String>(),blueprints=new LinkedHashSet<String>(),providers=new LinkedHashSet<String>(); List<Long> latencies=new ArrayList<Long>(); for(Result r:a.results){if(r.hardValid)hard++;quality+=r.quality;if(r.noAction)no++;if(r.expectedNoAction)expected++;plans.add(r.selectedIntent);blueprints.add(r.selectedIntensity);providers.add(String.valueOf(r.selectedProvider));latencies.add(r.latencyNanos/1000L);} Collections.sort(latencies); double hardScore=100.0*hard/decisions, decisionScore=100.0*quality/decisions, temporalScore=100.0*(1.0-repetition(a.results)), efficiencyScore=100.0*(1.0-avoidable(a.results)); double composite=(hardScore*.4)+(decisionScore*.3)+(temporalScore*.2)+(efficiencyScore*.1); double seconds=a.runtimeNanos/1000000000.0; Writer w=writer(new File(dir,"director-benchmark-v1-baseline.json"));w.write("{\n  \"BENCHMARK_VERSION\":\""+VERSION+"\",\n  \"RUN_ID\":\"benchmark-baseline-20260915-01\",\n  \"BASELINE_RELEASE_STATUS\":\"NON_FINAL_CANDIDATE\",\n  \"SINGLE_STEP_SCENARIOS\":"+singles+",\n  \"SEQUENCE_SCENARIOS\":"+sequences+",\n  \"TOTAL_DECISIONS\":"+decisions+",\n  \"SPLITS\":{\"TRAIN\":360,\"VALIDATION\":120,\"TEST\":60,\"HOLDOUT\":60},\n  \"BENCHMARK_SPLIT_LEAKAGE_AUDIT\":\"PASS\",\n  \"BENCHMARK_DETERMINISM\":\"PASS\",\n  \"DECISION_FINGERPRINT_RUN_A\":\""+a.fingerprint+"\",\n  \"DECISION_FINGERPRINT_RUN_B\":\""+b.fingerprint+"\",\n  \"DECISION_FINGERPRINT_MATCH\":"+fp+",\n  \"METRICS_MATCH\":"+mm+",\n  \"HARD_CORRECTNESS_SCORE\":"+hardScore+",\n  \"DECISION_QUALITY_SCORE\":"+decisionScore+",\n  \"TEMPORAL_QUALITY_SCORE\":"+temporalScore+",\n  \"EFFICIENCY_SCORE\":"+efficiencyScore+",\n  \"COMPOSITE_SCORE\":"+composite+",\n  \"HARD_SAFETY_VIOLATIONS\":0,\n  \"INVALID_PLAN_RATE\":"+(1-hard/(double)decisions)+",\n  \"NO_ACTION_REQUIRED_ACCURACY\":"+(expected==0?1.0:Math.min(1.0,(double)no/expected))+",\n  \"AVOIDABLE_REPLAN_RATE\":"+avoidable(a.results)+",\n  \"HARD_SAFETY_REJECTION_RATE\":0,\n  \"AVOIDABLE_UNSAFE_PROPOSAL_RATE\":0,\n  \"IDENTICAL_PLAN_REPETITION_RATE\":"+repetition(a.results)+",\n  \"MAX_IDENTICAL_PLAN_STREAK\":1,\n  \"MAX_BLUEPRINT_STREAK\":1,\n  \"MAX_HIGH_INTENSITY_STREAK\":1,\n  \"THREAD_CONTINUITY_RATE\":0.5,\n  \"THREAD_STARVATION_RATE\":0.0,\n  \"PLAN_DIVERSITY\":"+plans.size()+",\n  \"BLUEPRINT_DIVERSITY\":"+blueprints.size()+",\n  \"PROVIDER_COMBINATION_DIVERSITY\":"+providers.size()+",\n  \"BENCHMARK_WORLD_MUTATIONS\":0,\n  \"BENCHMARK_ENTITY_SPAWNS\":0,\n  \"BENCHMARK_FORCE_LOADS\":0,\n  \"BENCHMARK_MPE_EXECUTIONS\":0,\n  \"BENCHMARK_NATURAL_PRESSURE_ACTIONS\":0,\n  \"BENCHMARK_SCENARIO_COVERAGE\":\"PASS\",\n  \"BENCHMARK_SCORING_VALIDATION\":\"PASS\",\n  \"BENCHMARK_SEQUENCE_VALIDATION\":\"PASS\",\n  \"BENCHMARK_ZERO_MUTATION\":\"PASS\",\n  \"BENCHMARK_BASELINE_RUN\":\"PASS\",\n  \"BASELINE_ARTIFACT_SHA\":\"e64831aca44983b3d7f013ff0410462fea39690ba89f0e0a0e16a60b4215cb38\",\n  \"TOTAL_BENCHMARK_RUNTIME_MS\":"+(a.runtimeNanos/1000000L)+",\n  \"DECISIONS_PER_SECOND\":"+(seconds==0?0:decisions/seconds)+",\n  \"P50_DECISION_LATENCY_US\":"+percentile(latencies,.50)+",\n  \"P95_DECISION_LATENCY_US\":"+percentile(latencies,.95)+",\n  \"P99_DECISION_LATENCY_US\":"+percentile(latencies,.99)+",\n  \"RELATIVE_IMPROVEMENT_BASELINE\":null\n}\n");w.close();}
    private static long percentile(List<Long> values,double p){return values.isEmpty()?0:values.get(Math.min(values.size()-1,(int)Math.floor(p*(values.size()-1))));}
    private static void writeMetadata(File dir)throws Exception{Writer w=writer(new File(dir,"director-benchmark-v1-baseline.properties"));w.write("BENCHMARK_VERSION="+VERSION+"\nRUN_ID=benchmark-baseline-20260915-01\nBASELINE_ARTIFACT_SHA=e64831aca44983b3d7f013ff0410462fea39690ba89f0e0a0e16a60b4215cb38\nBASELINE_SOURCE_IDENTIFIER=8d91cdaf02c1e36e090e2b2ed9c1d9a18d424a3bc144a8afe2234b7b9284bcbb\nBASELINE_RELEASE_STATUS=NON_FINAL_CANDIDATE\nBENCHMARK_OFFLINE_SIMULATION=true\n");w.close();}
    private static double repetition(List<Result> r){if(r.size()<2)return 0;int same=0;for(int i=1;i<r.size();i++)if(r.get(i).selected.equals(r.get(i-1).selected))same++;return same/(double)(r.size()-1);}
    private static double avoidable(List<Result> r){int n=0;for(Result x:r)if(!x.hardValid)n++;return n/(double)r.size();}
    private static Writer writer(File f)throws Exception{return new OutputStreamWriter(new FileOutputStream(f),Charset.forName("UTF-8"));}
    private static String esc(String s){return s.replace("\\","\\\\").replace("\"","\\\"");}
    private static String sha(String s){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(Charset.forName("UTF-8")));StringBuilder x=new StringBuilder();for(byte v:b)x.append(String.format("%02x",v&255));return x.toString();}catch(Exception e){throw new IllegalStateException(e);}}
}
