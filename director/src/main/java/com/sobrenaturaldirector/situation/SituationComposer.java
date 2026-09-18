package com.sobrenaturaldirector.situation;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Collections;
import com.sobrenaturaldirector.action.NarrativeActionBridge;
import com.sobrenaturaldirector.action.SemanticActionCatalog;
import com.sobrenaturaldirector.control.DimensionRef;

/** Converts an already evaluated narrative decision into one executable instance. */
public final class SituationComposer {
    public SituationInstance compose(SituationDecision decision, DimensionRef dimension,
            SemanticActionCatalog catalog, String threadId) {
        if (decision == null || decision.getSelected() == null || decision.getSelectedBlueprint() == null ||
                !decision.getSelectedBlueprint().isFeasible() || dimension == null || catalog == null)
            throw new IllegalArgumentException("decision is not executable");
        SituationBlueprint blueprint=decision.getSelectedBlueprint().getBlueprint();
        String fingerprint=decision.getSelected().getGoal().name()+"|"+blueprint.fingerprint()+"|"+decision.getContext().getDimension()+"|"+
                decision.getContext().getRegionX()+"|"+decision.getContext().getRegionZ()+"|"+decision.getPlan().signature();
        String id="situation-"+digest(fingerprint);
        String planId="situation-plan-"+digest(fingerprint);
        return new SituationInstance(id, decision.getSelected().getGoal(), blueprint, threadId, dimension, dimension,
                digest(decision.getContext().getTick()+"|"+decision.getContext().getSeed()+"|"+decision.getContext().getEnvironment()),
                decision.getPlan().getProviders().toString(), catalog.fingerprint(),
                new NarrativeActionBridge().composePlan(blueprint,dimension,planId,catalog), decision.getContext().getTick())
                .transition(SituationLifecycleState.READY,"validated narrative composition",decision.getContext().getTick(),"PREFLIGHT");
    }
    private static String digest(String value) {
        try { MessageDigest md=MessageDigest.getInstance("SHA-256"); byte[] bytes=md.digest(value.getBytes(Charset.forName("UTF-8")));
            StringBuilder out=new StringBuilder(); for(byte b:bytes) out.append(String.format(java.util.Locale.ENGLISH,"%02x",b&255)); return out.substring(0,16);
        } catch(Exception e) { throw new IllegalStateException(e); }
    }
}
