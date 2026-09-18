package com.sobrenaturaldirector.action;

import java.util.*;
import com.sobrenaturaldirector.decision.model.*;

/** Backward-compatible adapter from semantic definitions to the V4 candidate model. */
public final class SemanticActionCandidateAdapter {
    private SemanticActionCandidateAdapter() { }
    public static CandidateAction toCandidate(ActionDefinition definition, double utility) {
        if (definition == null) throw new IllegalArgumentException("definition");
        Map<String,String> metadata=new LinkedHashMap<String,String>();metadata.put("semanticActionId",definition.getId());metadata.put("actionFamily",definition.getFamily().name());metadata.put("actionKind",definition.getKind().name());metadata.put("actionFingerprint",definition.fingerprint());
        Map<String,String> requirements=new LinkedHashMap<String,String>();if(!definition.getCapabilities().isEmpty())requirements.put("capabilities",join(definition.getCapabilities()));
        return new CandidateAction("action:"+definition.getId().toLowerCase(Locale.ENGLISH),intent(definition.getType()),definition.getFamily().name(),utility,requirements,metadata,definition.getCosts(),definition.getRisk());
    }
    private static String join(Collection<?> values){StringBuilder s=new StringBuilder();for(Object v:values){if(s.length()>0)s.append(',');s.append(v.toString());}return s.toString();}
    private static Intent intent(ActionType type){switch(type){case NO_ACTION:return Intent.NO_ACTION;case CREATE_HINT:return Intent.AMBIENT_HINT;case SPAWN_THREAT:case DESPAWN_THREAT:case CREATE_THREAT_PRESENCE:return Intent.MINOR_ENCOUNTER;case AMBUSH:return Intent.AMBUSH;case BOSS_ENCOUNTER:return Intent.BOSS_ENCOUNTER;case ENTER_RECOVERY_WINDOW:return Intent.RECOVERY;case PLACE_STRUCTURE:case ROLLBACK_STRUCTURE:return Intent.STRUCTURE_SEED;case CREATE_INVESTIGATION_SITE:return Intent.MYSTERY_SETUP;default:return Intent.MYSTERY_SETUP;}}
}
