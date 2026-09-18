package com.sobrenaturaldirector.situation;

import com.sobrenaturaldirector.narrative.NarrativeThreadManager;
import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;

/** Exactly-once persistence boundary for situation identity and thread membership. */
public final class SituationPersistenceCoordinator {
    private final NarrativeThreadManager threads = new NarrativeThreadManager();
    public SituationInstance save(DirectorWorldSavedData data, SituationInstance instance) {
        if (data == null || instance == null) throw new IllegalArgumentException("persistence inputs");
        PersistentSituation record = new PersistentSituation(instance);
        if (data.getSituation(instance.getId()) == null) data.recordSituation(record); else data.updateSituation(record);
        if (instance.getThreadId().length() > 0 && data.getNarrativeThread(instance.getThreadId()) != null)
            threads.linkSituation(data, instance.getThreadId(), instance.getId(), instance.getLastTransitionTick(), "SITUATION_LINKED");
        return instance;
    }
    public SituationInstance restore(DirectorWorldSavedData data, String situationId) {
        if (data == null || situationId == null) return null;
        PersistentSituation record=data.getSituation(situationId); return record == null ? null : record.getInstance();
    }
    /** Unknown in-progress work is suspended, never replayed blindly after restart. */
    public SituationInstance reconcileAfterRestart(DirectorWorldSavedData data, String situationId, long tick) {
        SituationInstance value=restore(data,situationId); if(value==null) return null;
        if (value.getState() == SituationLifecycleState.EXECUTING) {
            value=value.transition(SituationLifecycleState.SUSPENDED,"restart interrupted execution; journal reconciliation required",tick,"RESTART_RECOVERY");
            save(data,value);
        }
        return value;
    }
}
