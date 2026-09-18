package com.sobrenaturaldirector.action;

import java.util.*;
import com.sobrenaturaldirector.content.catalog.SemanticCatalog;
import com.sobrenaturaldirector.decision.model.*;
import com.sobrenaturaldirector.decision.candidate.CandidateGenerator;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;
import com.sobrenaturaldirector.situation.SituationContext;

/** Generates legacy-compatible candidates from the shared action catalog. */
public final class ActionCandidateGenerator {
    public List<CandidateAction> generate(DecisionContext context,SituationContext situation,DirectorProviderRegistry registry,SemanticCatalog content,SemanticActionCatalog catalog){
        if(context==null||registry==null||catalog==null)return Collections.emptyList();List<CandidateAction> result=new ArrayList<CandidateAction>();
        for(ActionDefinition d:catalog.getDefinitions().values()){ActionAvailability a=catalog.availability(d.getId(),context,situation,registry,content);if(a.isAvailable())result.add(SemanticActionCandidateAdapter.toCandidate(d,utility(d,context)));}
        Collections.sort(result,new Comparator<CandidateAction>(){public int compare(CandidateAction a,CandidateAction b){return a.getCandidateId().compareTo(b.getCandidateId());}});
        if(result.size()>CandidateGenerator.MAX_CANDIDATES){CandidateAction no=null;for(CandidateAction c:result)if(c.getIntent()==Intent.NO_ACTION)no=c;List<CandidateAction> bounded=new ArrayList<CandidateAction>();if(no!=null)bounded.add(no);for(CandidateAction c:result)if(c!=no&&bounded.size()<CandidateGenerator.MAX_CANDIDATES)bounded.add(c);result=bounded;Collections.sort(result,new Comparator<CandidateAction>(){public int compare(CandidateAction a,CandidateAction b){return a.getCandidateId().compareTo(b.getCandidateId());}});}
        return Collections.unmodifiableList(result);
    }
    private double utility(ActionDefinition d,DecisionContext c){if(d.getType()==ActionType.NO_ACTION)return .52+c.getRecoveryNeed()*.18;if(d.getFamily()==ActionFamily.THREAT)return .35+c.getTension()*.2;if(d.getFamily()==ActionFamily.INVESTIGATION)return .4+c.getIsolation()*.1;return .25;}
}
