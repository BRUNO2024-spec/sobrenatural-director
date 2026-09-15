package com.sobrenaturaldirector.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;
import com.sobrenaturaldirector.mutation.runtime.MutationJournalEntry;
import com.sobrenaturaldirector.composition.CompositionJournalEntry;
import com.sobrenaturaldirector.threat.ThreatJournalEntry;
import com.sobrenaturaldirector.threat.PlanExecutionRecord;
import com.sobrenaturaldirector.threat.PersistentNarrativeThreat;
import com.sobrenaturaldirector.threat.ThreatPhysicalBinding;
import com.sobrenaturaldirector.domain.DirectorWorldState;
import com.sobrenaturaldirector.naturalpressure.NaturalThreatPressureCoordinator;
import com.sobrenaturaldirector.naturalpressure.NaturalThreatPressureDirective;
import com.sobrenaturaldirector.naturalpressure.NaturalThreatPressureResolution;
import com.sobrenaturaldirector.execution.CoordinatedExecutionRecord;
import com.sobrenaturaldirector.execution.ExecutionOutcome;
import com.sobrenaturaldirector.narrative.PersistentNarrativePlan;
import com.sobrenaturaldirector.narrative.PersistentNarrativeThread;

/** World-scoped Director state and the bounded Phase 1K mutation journal. */
public final class DirectorWorldSavedData extends WorldSavedData {
    private DirectorWorldState state;
    private LoadStatus loadStatus;
    private NBTTagCompound preservedPayload;
    private final List<MutationJournalEntry> mutationJournal = new ArrayList<MutationJournalEntry>();
    private final List<CompositionJournalEntry> compositionJournal = new ArrayList<CompositionJournalEntry>();
    private final List<ThreatJournalEntry> threatJournal = new ArrayList<ThreatJournalEntry>();
    private final List<PersistentNarrativeThreat> narrativeThreats = new ArrayList<PersistentNarrativeThreat>();
    private final List<ThreatPhysicalBinding> threatBindings = new ArrayList<ThreatPhysicalBinding>();
    private final List<PlanExecutionRecord> planExecutionLedger = new ArrayList<PlanExecutionRecord>();
    private final List<CoordinatedExecutionRecord> coordinatedExecutionLedger = new ArrayList<CoordinatedExecutionRecord>();
    private final List<ExecutionOutcome> executionOutcomes = new ArrayList<ExecutionOutcome>();
    private final List<PersistentNarrativePlan> narrativePlans = new ArrayList<PersistentNarrativePlan>();
    private final List<PersistentNarrativeThread> narrativeThreads = new ArrayList<PersistentNarrativeThread>();
    private final List<com.sobrenaturaldirector.narrative.PacingHistoryEntry> pacingHistory = new ArrayList<com.sobrenaturaldirector.narrative.PacingHistoryEntry>();
    private final Map<String, SchedulerMetadata> schedulerMetadata = new LinkedHashMap<String, SchedulerMetadata>();
    private final NaturalThreatPressureCoordinator naturalPressure = new NaturalThreatPressureCoordinator();

    public DirectorWorldSavedData() { this(DirectorPersistenceMetadata.DATA_NAME); }
    public DirectorWorldSavedData(String name) { super(name); state = DirectorWorldState.empty(0L); loadStatus = LoadStatus.FRESH; }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        if (nbt == null || !nbt.hasKey(DirectorStateNbtCodec.SCHEMA)) { fail(LoadStatus.MISSING_SCHEMA, nbt); return; }
        if (!nbt.hasKey(DirectorStateNbtCodec.SCHEMA, 3)) { fail(LoadStatus.CORRUPT, nbt); return; }
        int schema = nbt.getInteger(DirectorStateNbtCodec.SCHEMA);
        if (schema > DirectorPersistenceMetadata.CURRENT_SCHEMA_VERSION) { fail(LoadStatus.UNSUPPORTED_FUTURE_SCHEMA, nbt); return; }
        if (schema < DirectorPersistenceMetadata.CURRENT_SCHEMA_VERSION) { fail(LoadStatus.UNSUPPORTED_OLD_SCHEMA, nbt); return; }
         try { DirectorWorldState decoded = DirectorStateNbtCodec.decode(nbt); state = decoded; readJournal(nbt); readThreatJournal(nbt); readNarrativeThreats(nbt); readThreatBindings(nbt); readPlanExecutionLedger(nbt); readCoordinatedExecutionLedger(nbt); readExecutionOutcomes(nbt); readNaturalPressure(nbt); readNarrativePlans(nbt); readNarrativeThreads(nbt); readPacingHistory(nbt); readSchedulerMetadata(nbt); loadStatus = LoadStatus.LOADED; preservedPayload = null; }
        catch (RuntimeException exception) { fail(LoadStatus.CORRUPT, nbt); }
    }

    @Override public void writeToNBT(NBTTagCompound nbt) {
        if (!isWritable()) { copyInto(nbt, preservedPayload); return; }
        copyInto(nbt, DirectorStateNbtCodec.encode(state));
        NBTTagList journal = new NBTTagList(); for (MutationJournalEntry entry : mutationJournal) journal.appendTag(entry.toNbt()); nbt.setTag("mutationJournal", journal);
        NBTTagList compositions = new NBTTagList(); for (CompositionJournalEntry entry : compositionJournal) compositions.appendTag(entry.toNbt()); nbt.setTag("compositionJournal", compositions);
        NBTTagList threats = new NBTTagList(); for (ThreatJournalEntry entry : threatJournal) threats.appendTag(entry.toNbt()); nbt.setTag("threatJournal", threats);
        NBTTagList narratives = new NBTTagList(); for (PersistentNarrativeThreat entry : narrativeThreats) narratives.appendTag(entry.toNbt()); nbt.setTag("narrativeThreats", narratives);
        NBTTagList bindings = new NBTTagList(); for (ThreatPhysicalBinding entry : threatBindings) bindings.appendTag(entry.toNbt()); nbt.setTag("threatBindings", bindings);
        NBTTagList executions = new NBTTagList(); for (PlanExecutionRecord entry : planExecutionLedger) executions.appendTag(entry.toNbt()); nbt.setTag("planExecutionLedger", executions);
         NBTTagList coordinated = new NBTTagList(); for (CoordinatedExecutionRecord entry : coordinatedExecutionLedger) coordinated.appendTag(entry.toNbt()); nbt.setTag("coordinatedExecutionLedger", coordinated);
         NBTTagList outcomes = new NBTTagList(); for (ExecutionOutcome entry : executionOutcomes) { NBTTagCompound item=entryNbt(entry); outcomes.appendTag(item); } nbt.setTag("executionOutcomes", outcomes);
        NBTTagList pressures = new NBTTagList(); for (NaturalThreatPressureDirective entry : naturalPressure.getDirectives()) pressures.appendTag(entry.toNbt()); nbt.setTag("naturalPressure", pressures);
        NBTTagList plans = new NBTTagList(); for (PersistentNarrativePlan entry : narrativePlans) plans.appendTag(entry.toNbt()); nbt.setTag("narrativePlans", plans);
        NBTTagList threads = new NBTTagList(); for (PersistentNarrativeThread entry : narrativeThreads) threads.appendTag(entry.toNbt()); nbt.setTag("narrativeThreads", threads);
        NBTTagList pacing = new NBTTagList(); for (com.sobrenaturaldirector.narrative.PacingHistoryEntry entry : pacingHistory) pacing.appendTag(entry.toNbt()); nbt.setTag("pacingHistory", pacing);
        NBTTagList scheduler = new NBTTagList(); for (Map.Entry<String, SchedulerMetadata> entry : schedulerMetadata.entrySet()) { NBTTagCompound item = new NBTTagCompound(); item.setString("scope", entry.getKey()); item.setLong("lastTick", entry.getValue().lastTick); item.setLong("backoffTicks", entry.getValue().backoffTicks); item.setString("fingerprint", entry.getValue().fingerprint); scheduler.appendTag(item); } nbt.setTag("schedulerMetadata", scheduler);
    }

    public DirectorWorldState getState() { return state; }
    public LoadStatus getLoadStatus() { return loadStatus; }
    public boolean isWritable() { return loadStatus == LoadStatus.FRESH || loadStatus == LoadStatus.LOADED; }
    public NBTTagCompound getPreservedPayload() { return preservedPayload == null ? null : (NBTTagCompound) preservedPayload.copy(); }

    public void replaceState(DirectorWorldState replacement) {
        if (replacement == null) throw new IllegalArgumentException("replacement state is required");
        if (!isWritable()) throw new IllegalStateException("persistence state is quarantined");
        state = replacement; touchDirty();
    }

    public List<MutationJournalEntry> getMutationJournal() { return Collections.unmodifiableList(new ArrayList<MutationJournalEntry>(mutationJournal)); }
    public MutationJournalEntry getMutation(String id) { for (MutationJournalEntry entry : mutationJournal) if (entry.getMutationId().equals(id)) return entry; return null; }
    public boolean hasExecutedMutation(String id) { MutationJournalEntry entry = getMutation(id); return entry != null && ("EXECUTED".equals(entry.getStatus()) || "ROLLED_BACK".equals(entry.getStatus())); }
    public void recordMutation(MutationJournalEntry entry) { if (entry == null || getMutation(entry.getMutationId()) != null) throw new IllegalArgumentException("duplicate mutation journal id"); if (mutationJournal.size() >= DirectorWorldState.MAX_RECORDS) throw new IllegalStateException("mutation journal limit reached"); mutationJournal.add(entry); touchDirty(); }
    public void updateMutation(MutationJournalEntry replacement) { if (replacement == null) throw new IllegalArgumentException("journal entry is required"); for (int i=0;i<mutationJournal.size();i++) if (mutationJournal.get(i).getMutationId().equals(replacement.getMutationId())) { mutationJournal.set(i, replacement); touchDirty(); return; } throw new IllegalArgumentException("mutation journal id not found"); }
    public List<CompositionJournalEntry> getCompositionJournal() { return Collections.unmodifiableList(new ArrayList<CompositionJournalEntry>(compositionJournal)); }
    public CompositionJournalEntry getComposition(String id) { for (CompositionJournalEntry entry : compositionJournal) if (entry.getId().equals(id)) return entry; return null; }
    public void recordComposition(CompositionJournalEntry entry) { if (entry == null || getComposition(entry.getId()) != null) throw new IllegalArgumentException("duplicate composition journal id"); if (compositionJournal.size() >= DirectorWorldState.MAX_RECORDS) throw new IllegalStateException("composition journal limit reached"); compositionJournal.add(entry); touchDirty(); }
    public void updateComposition(CompositionJournalEntry replacement) { for (int i=0;i<compositionJournal.size();i++) if (compositionJournal.get(i).getId().equals(replacement.getId())) { compositionJournal.set(i, replacement); touchDirty(); return; } throw new IllegalArgumentException("composition journal id not found"); }
    public List<ThreatJournalEntry> getThreatJournal() { return Collections.unmodifiableList(new ArrayList<ThreatJournalEntry>(threatJournal)); }
    public ThreatJournalEntry getThreat(String requestId) { for (ThreatJournalEntry entry : threatJournal) if (entry.getRequestId().equals(requestId)) return entry; return null; }
    public void recordThreat(ThreatJournalEntry entry) { if (entry == null || getThreat(entry.getRequestId()) != null) throw new IllegalArgumentException("duplicate threat journal id"); if (threatJournal.size() >= DirectorWorldState.MAX_RECORDS) throw new IllegalStateException("threat journal limit reached"); threatJournal.add(entry); touchDirty(); }
    public void updateThreat(ThreatJournalEntry replacement) { if (replacement == null) throw new IllegalArgumentException("threat journal entry is required"); for (int i=0;i<threatJournal.size();i++) if (threatJournal.get(i).getRequestId().equals(replacement.getRequestId())) { threatJournal.set(i, replacement); touchDirty(); return; } throw new IllegalArgumentException("threat journal id not found"); }
    public List<PersistentNarrativeThreat> getNarrativeThreats() { return Collections.unmodifiableList(new ArrayList<PersistentNarrativeThreat>(narrativeThreats)); }
    public PersistentNarrativeThreat getNarrativeThreat(String id) { for (PersistentNarrativeThreat entry : narrativeThreats) if (entry.getId().equals(id)) return entry; return null; }
    public void recordNarrativeThreat(PersistentNarrativeThreat entry) { if (entry == null || getNarrativeThreat(entry.getId()) != null) throw new IllegalArgumentException("duplicate narrative threat id"); if (narrativeThreats.size() >= DirectorWorldState.MAX_RECORDS) throw new IllegalStateException("narrative threat limit reached"); narrativeThreats.add(entry); touchDirty(); }
    public void updateNarrativeThreat(PersistentNarrativeThreat replacement) { if (replacement == null) throw new IllegalArgumentException("narrative threat is required"); for (int i=0;i<narrativeThreats.size();i++) if (narrativeThreats.get(i).getId().equals(replacement.getId())) { narrativeThreats.set(i, replacement); touchDirty(); return; } throw new IllegalArgumentException("narrative threat id not found"); }
    public List<ThreatPhysicalBinding> getThreatBindings() { return Collections.unmodifiableList(new ArrayList<ThreatPhysicalBinding>(threatBindings)); }
    public ThreatPhysicalBinding getThreatBinding(String threatId) { for (ThreatPhysicalBinding entry : threatBindings) if (entry.getThreatId().equals(threatId)) return entry; return null; }
    public void recordThreatBinding(ThreatPhysicalBinding entry) { if (entry == null || getThreatBinding(entry.getThreatId()) != null) throw new IllegalArgumentException("duplicate threat binding id"); if (threatBindings.size() >= DirectorWorldState.MAX_RECORDS) throw new IllegalStateException("threat binding limit reached"); threatBindings.add(entry); touchDirty(); }
    public void updateThreatBinding(ThreatPhysicalBinding replacement) { if (replacement == null) throw new IllegalArgumentException("threat binding is required"); for (int i=0;i<threatBindings.size();i++) if (threatBindings.get(i).getThreatId().equals(replacement.getThreatId())) { threatBindings.set(i, replacement); touchDirty(); return; } throw new IllegalArgumentException("threat binding id not found"); }
    public List<PlanExecutionRecord> getPlanExecutionLedger() { return Collections.unmodifiableList(new ArrayList<PlanExecutionRecord>(planExecutionLedger)); }
    public PlanExecutionRecord getPlanExecution(String executionId) { for (PlanExecutionRecord entry : planExecutionLedger) if (entry.getExecutionId().equals(executionId)) return entry; return null; }
    public void recordPlanExecution(PlanExecutionRecord entry) { if (entry == null || getPlanExecution(entry.getExecutionId()) != null) throw new IllegalArgumentException("duplicate execution ledger id"); if (planExecutionLedger.size() >= DirectorWorldState.MAX_RECORDS) throw new IllegalStateException("execution ledger limit reached"); planExecutionLedger.add(entry); touchDirty(); }
    public void updatePlanExecution(PlanExecutionRecord replacement) { if (replacement == null) throw new IllegalArgumentException("execution ledger entry is required"); for (int i=0;i<planExecutionLedger.size();i++) if (planExecutionLedger.get(i).getExecutionId().equals(replacement.getExecutionId())) { planExecutionLedger.set(i, replacement); touchDirty(); return; } throw new IllegalArgumentException("execution ledger id not found"); }
    public CoordinatedExecutionRecord getCoordinatedExecution(String executionId) { for (CoordinatedExecutionRecord entry : coordinatedExecutionLedger) if (entry.getExecutionId().equals(executionId)) return entry; return null; }
    public void recordCoordinatedExecution(CoordinatedExecutionRecord entry) { if (entry == null || getCoordinatedExecution(entry.getExecutionId()) != null) throw new IllegalArgumentException("duplicate coordinated execution id"); if (coordinatedExecutionLedger.size() >= DirectorWorldState.MAX_RECORDS) throw new IllegalStateException("coordinated execution ledger limit reached"); coordinatedExecutionLedger.add(entry); touchDirty(); }
     public void updateCoordinatedExecution(CoordinatedExecutionRecord replacement) { for (int i=0;i<coordinatedExecutionLedger.size();i++) if (coordinatedExecutionLedger.get(i).getExecutionId().equals(replacement.getExecutionId())) { coordinatedExecutionLedger.set(i, replacement); touchDirty(); return; } throw new IllegalArgumentException("coordinated execution ledger id not found"); }
     public List<ExecutionOutcome> getExecutionOutcomes() { return Collections.unmodifiableList(new ArrayList<ExecutionOutcome>(executionOutcomes)); }
     public ExecutionOutcome getExecutionOutcome(String id) { for (ExecutionOutcome entry:executionOutcomes) if (entry.getOutcomeId().equals(id)) return entry; return null; }
     public void recordExecutionOutcome(ExecutionOutcome entry) { if (entry==null||getExecutionOutcome(entry.getOutcomeId())!=null) throw new IllegalArgumentException("duplicate outcome id"); if(executionOutcomes.size()>=DirectorWorldState.MAX_RECORDS) throw new IllegalStateException("execution outcome limit reached"); executionOutcomes.add(entry); touchDirty(); }
     public void updateExecutionOutcome(ExecutionOutcome replacement) { for(int i=0;i<executionOutcomes.size();i++) if(executionOutcomes.get(i).getOutcomeId().equals(replacement.getOutcomeId())) { executionOutcomes.set(i,replacement); touchDirty(); return; } throw new IllegalArgumentException("execution outcome id not found"); }
    public void putNaturalPressure(NaturalThreatPressureDirective directive) { if(!isWritable())throw new IllegalStateException("persistence state is quarantined"); naturalPressure.put(directive); touchDirty(); }
    public boolean removeNaturalPressure(String directiveId) { boolean removed=naturalPressure.remove(directiveId); if(removed)touchDirty(); return removed; }
    public NaturalThreatPressureResolution resolveNaturalPressure(String provider,String category,int dimension,int chunkX,int chunkZ,long tick,boolean available) { return naturalPressure.resolve(provider,category,dimension,chunkX,chunkZ,tick,available); }
    public List<NaturalThreatPressureDirective> getNaturalPressure() { return naturalPressure.getDirectives(); }
    public List<PersistentNarrativePlan> getNarrativePlans() { return Collections.unmodifiableList(new ArrayList<PersistentNarrativePlan>(narrativePlans)); }
    public PersistentNarrativePlan getNarrativePlan(String id) { for (PersistentNarrativePlan entry : narrativePlans) if (entry.getId().equals(id)) return entry; return null; }
    public void recordNarrativePlan(PersistentNarrativePlan entry) { if (entry == null || getNarrativePlan(entry.getId()) != null) throw new IllegalArgumentException("duplicate narrative plan id"); if (narrativePlans.size() >= 64) throw new IllegalStateException("narrative plan limit reached"); narrativePlans.add(entry); touchDirty(); }
    public void updateNarrativePlan(PersistentNarrativePlan replacement) { if (replacement == null) throw new IllegalArgumentException("narrative plan is required"); for (int i=0;i<narrativePlans.size();i++) if (narrativePlans.get(i).getId().equals(replacement.getId())) { narrativePlans.set(i, replacement); touchDirty(); return; } throw new IllegalArgumentException("narrative plan id not found"); }
    public List<PersistentNarrativeThread> getNarrativeThreads() { return Collections.unmodifiableList(new ArrayList<PersistentNarrativeThread>(narrativeThreads)); }
    public PersistentNarrativeThread getNarrativeThread(String id) { for (PersistentNarrativeThread entry : narrativeThreads) if (entry.getId().equals(id)) return entry; return null; }
    public void recordNarrativeThread(PersistentNarrativeThread entry) { if (entry == null || getNarrativeThread(entry.getId()) != null) throw new IllegalArgumentException("duplicate narrative thread id"); if (narrativeThreads.size() >= 16) throw new IllegalStateException("narrative thread limit reached"); narrativeThreads.add(entry); touchDirty(); }
    public void updateNarrativeThread(PersistentNarrativeThread replacement) { if (replacement == null) throw new IllegalArgumentException("narrative thread is required"); for (int i=0;i<narrativeThreads.size();i++) if (narrativeThreads.get(i).getId().equals(replacement.getId())) { narrativeThreads.set(i, replacement); touchDirty(); return; } throw new IllegalArgumentException("narrative thread id not found"); }
    public List<com.sobrenaturaldirector.narrative.PacingHistoryEntry> getPacingHistory() { return Collections.unmodifiableList(new ArrayList<com.sobrenaturaldirector.narrative.PacingHistoryEntry>(pacingHistory)); }
    public void recordPacing(com.sobrenaturaldirector.narrative.PacingHistoryEntry entry) { if (entry == null) return; if (pacingHistory.size() >= 64) pacingHistory.remove(0); pacingHistory.add(entry); touchDirty(); }
    public SchedulerMetadata getSchedulerMetadata(String scope) { return schedulerMetadata.get(scope); }
    public void setSchedulerMetadata(String scope, long lastTick, String fingerprint) { setSchedulerMetadata(scope,lastTick,fingerprint,200L); }
    public long getSchedulerBackoff(String scope) { SchedulerMetadata value=schedulerMetadata.get(scope); return value==null?200L:value.backoffTicks; }
    public void setSchedulerMetadata(String scope, long lastTick, String fingerprint, long backoffTicks) { if (scope == null || scope.length() == 0 || fingerprint == null || backoffTicks < 1) throw new IllegalArgumentException("scheduler metadata is invalid"); if (schedulerMetadata.size() >= 64 && !schedulerMetadata.containsKey(scope)) throw new IllegalStateException("scheduler metadata limit reached"); schedulerMetadata.put(scope, new SchedulerMetadata(lastTick, fingerprint, backoffTicks)); touchDirty(); }

    private void fail(LoadStatus status, NBTTagCompound raw) { loadStatus = status; preservedPayload = raw == null ? null : (NBTTagCompound) raw.copy(); state = DirectorWorldState.empty(0L); try { WorldSavedData.class.getMethod("setDirty", Boolean.TYPE).invoke(this, Boolean.FALSE); } catch (Exception ignored) { } }
    private void touchDirty() { try { WorldSavedData.class.getMethod("func_76185_a").invoke(this); } catch (Exception ignored) { try { WorldSavedData.class.getMethod("setDirty", Boolean.TYPE).invoke(this, Boolean.TRUE); } catch (Exception ignoredAgain) { } }
    }
    private void readJournal(NBTTagCompound nbt) { mutationJournal.clear(); compositionJournal.clear(); if (nbt.hasKey("mutationJournal", 9)) { NBTTagList list = nbt.getTagList("mutationJournal", 10); if (list.tagCount() > DirectorWorldState.MAX_RECORDS) throw new IllegalArgumentException("mutation journal exceeds record limit"); for (int i=0;i<list.tagCount();i++) { MutationJournalEntry entry = MutationJournalEntry.fromNbt(list.getCompoundTagAt(i)); if (getMutation(entry.getMutationId()) != null) throw new IllegalArgumentException("duplicate mutation journal id"); mutationJournal.add(entry); } } if (nbt.hasKey("compositionJournal", 9)) { NBTTagList list = nbt.getTagList("compositionJournal", 10); if (list.tagCount() > DirectorWorldState.MAX_RECORDS) throw new IllegalArgumentException("composition journal exceeds record limit"); for (int i=0;i<list.tagCount();i++) { CompositionJournalEntry entry = CompositionJournalEntry.fromNbt(list.getCompoundTagAt(i)); if (getComposition(entry.getId()) != null) throw new IllegalArgumentException("duplicate composition journal id"); compositionJournal.add(entry); } } }
    private void readThreatJournal(NBTTagCompound nbt) { threatJournal.clear(); if (!nbt.hasKey("threatJournal", 9)) return; NBTTagList list = nbt.getTagList("threatJournal", 10); if (list.tagCount() > DirectorWorldState.MAX_RECORDS) throw new IllegalArgumentException("threat journal exceeds record limit"); for (int i=0;i<list.tagCount();i++) { ThreatJournalEntry entry = ThreatJournalEntry.fromNbt(list.getCompoundTagAt(i)); if (getThreat(entry.getRequestId()) != null) throw new IllegalArgumentException("duplicate threat journal id"); threatJournal.add(entry); } }
    private void readNarrativeThreats(NBTTagCompound nbt) { narrativeThreats.clear(); if (!nbt.hasKey("narrativeThreats", 9)) return; NBTTagList list = nbt.getTagList("narrativeThreats", 10); if (list.tagCount() > DirectorWorldState.MAX_RECORDS) throw new IllegalArgumentException("narrative threat limit exceeded"); for (int i=0;i<list.tagCount();i++) { PersistentNarrativeThreat entry = PersistentNarrativeThreat.fromNbt(list.getCompoundTagAt(i)); if (getNarrativeThreat(entry.getId()) != null) throw new IllegalArgumentException("duplicate narrative threat id"); narrativeThreats.add(entry); } }
    private void readThreatBindings(NBTTagCompound nbt) { threatBindings.clear(); if (!nbt.hasKey("threatBindings", 9)) return; NBTTagList list = nbt.getTagList("threatBindings", 10); if (list.tagCount() > DirectorWorldState.MAX_RECORDS) throw new IllegalArgumentException("threat binding limit exceeded"); for (int i=0;i<list.tagCount();i++) { ThreatPhysicalBinding entry = ThreatPhysicalBinding.fromNbt(list.getCompoundTagAt(i)); if (getThreatBinding(entry.getThreatId()) != null) throw new IllegalArgumentException("duplicate threat binding id"); threatBindings.add(entry); } }
    private void readPlanExecutionLedger(NBTTagCompound nbt) { planExecutionLedger.clear(); if (!nbt.hasKey("planExecutionLedger", 9)) return; NBTTagList list = nbt.getTagList("planExecutionLedger", 10); if (list.tagCount() > DirectorWorldState.MAX_RECORDS) throw new IllegalArgumentException("execution ledger exceeds record limit"); for (int i=0;i<list.tagCount();i++) { PlanExecutionRecord entry = PlanExecutionRecord.fromNbt(list.getCompoundTagAt(i)); if (getPlanExecution(entry.getExecutionId()) != null) throw new IllegalArgumentException("duplicate execution ledger id"); planExecutionLedger.add(entry); } }
      private void readCoordinatedExecutionLedger(NBTTagCompound nbt) { coordinatedExecutionLedger.clear(); if (!nbt.hasKey("coordinatedExecutionLedger", 9)) return; NBTTagList list = nbt.getTagList("coordinatedExecutionLedger", 10); if (list.tagCount() > DirectorWorldState.MAX_RECORDS) throw new IllegalArgumentException("coordinated execution ledger exceeds record limit"); for (int i=0;i<list.tagCount();i++) { CoordinatedExecutionRecord entry = CoordinatedExecutionRecord.fromNbt(list.getCompoundTagAt(i)); if (getCoordinatedExecution(entry.getExecutionId()) != null) throw new IllegalArgumentException("duplicate coordinated execution id"); coordinatedExecutionLedger.add(entry); } }
      private void readExecutionOutcomes(NBTTagCompound nbt) { executionOutcomes.clear(); if(!nbt.hasKey("executionOutcomes",9))return; NBTTagList list=nbt.getTagList("executionOutcomes",10); if(list.tagCount()>DirectorWorldState.MAX_RECORDS)throw new IllegalArgumentException("execution outcomes exceed record limit"); for(int i=0;i<list.tagCount();i++){ExecutionOutcome entry=outcomeFromNbt(list.getCompoundTagAt(i));if(getExecutionOutcome(entry.getOutcomeId())!=null)throw new IllegalArgumentException("duplicate execution outcome id");executionOutcomes.add(entry);} }
      private static NBTTagCompound entryNbt(ExecutionOutcome e){NBTTagCompound n=new NBTTagCompound();n.setString("outcomeId",e.getOutcomeId());n.setString("executionId",e.getExecutionId());n.setString("planId",e.getPlanId());n.setString("providerId",e.getProviderId());n.setString("physicalIdentity",e.getPhysicalIdentity());n.setString("status",e.getStatus().name());n.setLong("tick",e.getTick());n.setString("reason",e.getReason());n.setBoolean("feedbackApplied",e.isFeedbackApplied());return n;}
      private static ExecutionOutcome outcomeFromNbt(NBTTagCompound n){return new ExecutionOutcome(n.getString("outcomeId"),n.getString("executionId"),n.getString("planId"),n.getString("providerId"),n.getString("physicalIdentity"),ExecutionOutcome.Status.valueOf(n.getString("status")),n.getLong("tick"),n.getString("reason"),n.getBoolean("feedbackApplied"));}
    private void readNaturalPressure(NBTTagCompound nbt) { naturalPressure.getDirectives(); if(!nbt.hasKey("naturalPressure",9))return; NBTTagList list=nbt.getTagList("naturalPressure",10); if(list.tagCount()>DirectorWorldState.MAX_RECORDS)throw new IllegalArgumentException("natural pressure exceeds record limit"); for(int i=0;i<list.tagCount();i++)naturalPressure.put(NaturalThreatPressureDirective.fromNbt(list.getCompoundTagAt(i))); }
    private void readNarrativePlans(NBTTagCompound nbt) { narrativePlans.clear(); if(!nbt.hasKey("narrativePlans",9))return; NBTTagList list=nbt.getTagList("narrativePlans",10); if(list.tagCount()>64)throw new IllegalArgumentException("narrative plan limit exceeded"); for(int i=0;i<list.tagCount();i++){PersistentNarrativePlan entry=PersistentNarrativePlan.fromNbt(list.getCompoundTagAt(i));if(getNarrativePlan(entry.getId())!=null)throw new IllegalArgumentException("duplicate narrative plan id");narrativePlans.add(entry);} }
     private void readNarrativeThreads(NBTTagCompound nbt) { narrativeThreads.clear(); if(!nbt.hasKey("narrativeThreads",9))return; NBTTagList list=nbt.getTagList("narrativeThreads",10); if(list.tagCount()>16)throw new IllegalArgumentException("narrative thread limit exceeded"); for(int i=0;i<list.tagCount();i++){PersistentNarrativeThread entry=PersistentNarrativeThread.fromNbt(list.getCompoundTagAt(i));if(getNarrativeThread(entry.getId())!=null)throw new IllegalArgumentException("duplicate narrative thread id");narrativeThreads.add(entry);} }
     private void readPacingHistory(NBTTagCompound nbt) { pacingHistory.clear(); if(!nbt.hasKey("pacingHistory",9))return; NBTTagList list=nbt.getTagList("pacingHistory",10); if(list.tagCount()>64)throw new IllegalArgumentException("pacing history limit exceeded"); for(int i=0;i<list.tagCount();i++)pacingHistory.add(com.sobrenaturaldirector.narrative.PacingHistoryEntry.fromNbt(list.getCompoundTagAt(i))); }
     private void readSchedulerMetadata(NBTTagCompound nbt) { schedulerMetadata.clear(); if(!nbt.hasKey("schedulerMetadata",9))return; NBTTagList list=nbt.getTagList("schedulerMetadata",10); if(list.tagCount()>64)throw new IllegalArgumentException("scheduler metadata limit exceeded"); for(int i=0;i<list.tagCount();i++){NBTTagCompound item=list.getCompoundTagAt(i);String scope=item.getString("scope");if(scope.length()==0||schedulerMetadata.containsKey(scope))throw new IllegalArgumentException("invalid scheduler metadata");long backoff=item.hasKey("backoffTicks",4)?item.getLong("backoffTicks"):200L;schedulerMetadata.put(scope,new SchedulerMetadata(item.getLong("lastTick"),item.getString("fingerprint"),backoff));} }
     public static final class SchedulerMetadata { private final long lastTick,backoffTicks; private final String fingerprint; private SchedulerMetadata(long lastTick,String fingerprint,long backoffTicks){this.lastTick=lastTick;this.fingerprint=fingerprint;this.backoffTicks=backoffTicks;} public long getLastTick(){return lastTick;} public String getFingerprint(){return fingerprint;} public long getBackoffTicks(){return backoffTicks;} }
    @SuppressWarnings("unchecked")
    private static void copyInto(NBTTagCompound target, NBTTagCompound source) { if (source == null) return; Set<String> keys = source.func_150296_c(); for (String name : keys) { NBTBase value = source.getTag(name); target.setTag(name, value.copy()); } }
}
