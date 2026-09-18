package com.sobrenaturaldirector.situation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.action.ActionPlan;
import com.sobrenaturaldirector.action.ActionOutcome;
import com.sobrenaturaldirector.control.DimensionRef;

/** Immutable/versioned runtime identity for one composed situation. */
public final class SituationInstance {
    public static final int SCHEMA_VERSION = 1;
    private final String id, threadId, contextFingerprint, providerFingerprint, contentFingerprint;
    private final SituationGoal goal;
    private final SituationBlueprint blueprint;
    private final DimensionRef originDimension, relevantDimension;
    private final ActionPlan actionPlan;
    private final SituationLifecycleState state;
    private final long creationTick, lastTransitionTick;
    private final ActionOutcome outcome;
    private final List<SituationLifecycleTransition> transitions;

    public SituationInstance(String id, SituationGoal goal, SituationBlueprint blueprint, String threadId,
            DimensionRef originDimension, DimensionRef relevantDimension, String contextFingerprint,
            String providerFingerprint, String contentFingerprint, ActionPlan actionPlan, long creationTick) {
        if (id == null || id.length() == 0 || goal == null || blueprint == null || originDimension == null ||
                relevantDimension == null || contextFingerprint == null || providerFingerprint == null ||
                contentFingerprint == null || actionPlan == null || creationTick < 0)
            throw new IllegalArgumentException("invalid situation instance");
        this.id=id; this.goal=goal; this.blueprint=blueprint; this.threadId=threadId == null ? "" : threadId;
        this.originDimension=originDimension; this.relevantDimension=relevantDimension;
        this.contextFingerprint=contextFingerprint; this.providerFingerprint=providerFingerprint;
        this.contentFingerprint=contentFingerprint; this.actionPlan=actionPlan; this.state=SituationLifecycleState.PLANNED;
        this.creationTick=creationTick; this.lastTransitionTick=creationTick; this.outcome=null;
        this.transitions=Collections.emptyList();
    }
    private SituationInstance(SituationInstance old, SituationLifecycleTransition transition, ActionOutcome outcome) {
        this.id=old.id; this.goal=old.goal; this.blueprint=old.blueprint; this.threadId=old.threadId;
        this.originDimension=old.originDimension; this.relevantDimension=old.relevantDimension;
        this.contextFingerprint=old.contextFingerprint; this.providerFingerprint=old.providerFingerprint;
        this.contentFingerprint=old.contentFingerprint; this.actionPlan=old.actionPlan; this.state=transition.getTo();
        this.creationTick=old.creationTick; this.lastTransitionTick=transition.getTick(); this.outcome=outcome == null ? old.outcome : outcome;
        ArrayList<SituationLifecycleTransition> all=new ArrayList<SituationLifecycleTransition>(old.transitions); all.add(transition);
        this.transitions=Collections.unmodifiableList(all);
    }
    public SituationInstance transition(SituationLifecycleState to, String reason, long tick, String trigger) {
        return new SituationInstance(this, SituationLifecycle.transition(state,to,reason,tick,trigger), outcome);
    }
    public SituationInstance withOutcome(ActionOutcome value, SituationLifecycleState to, String reason, long tick, String trigger) {
        return new SituationInstance(this, SituationLifecycle.transition(state,to,reason,tick,trigger), value);
    }
    public String getId(){return id;} public String getThreadId(){return threadId;} public SituationGoal getGoal(){return goal;}
    public SituationBlueprint getBlueprint(){return blueprint;} public DimensionRef getOriginDimension(){return originDimension;}
    public DimensionRef getRelevantDimension(){return relevantDimension;} public ActionPlan getActionPlan(){return actionPlan;}
    public SituationLifecycleState getState(){return state;} public long getCreationTick(){return creationTick;}
    public long getLastTransitionTick(){return lastTransitionTick;} public ActionOutcome getOutcome(){return outcome;}
    public String getContextFingerprint(){return contextFingerprint;} public String getProviderFingerprint(){return providerFingerprint;}
    public String getContentFingerprint(){return contentFingerprint;}
    public List<SituationLifecycleTransition> getTransitions(){return transitions;}
    public boolean isTerminal(){return state.isTerminal();}
}
