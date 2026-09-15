package com.sobrenaturaldirector.content.query;

import java.util.*;
import com.sobrenaturaldirector.content.model.*;

public final class ResolutionResult {
    public enum Status { RESOLVED,MULTIPLE_CANDIDATES,UNRESOLVED,PROVIDER_MISSING,CONTENT_MISSING,ADAPTER_REQUIRED,INSUFFICIENT_EVIDENCE,UNKNOWN }
    public static final class Candidate {private final SemanticContentEntry entry;private final AvailabilityStatus availability;private final ExecutionStatus execution;private final String reason;public Candidate(SemanticContentEntry e,AvailabilityStatus a,ExecutionStatus x,String r){entry=e;availability=a;execution=x;reason=r;}public SemanticContentEntry getEntry(){return entry;}public AvailabilityStatus getAvailability(){return availability;}public ExecutionStatus getExecution(){return execution;}public String getReason(){return reason;}}
    private final Status status;private final List<Candidate> candidates;private final String reason;private final int examined;
    public ResolutionResult(Status status,Collection<Candidate> candidates,String reason,int examined){this.status=status;this.candidates=Collections.unmodifiableList(new ArrayList<Candidate>(candidates==null?Collections.<Candidate>emptyList():candidates));this.reason=reason;this.examined=examined;}
    public Status getStatus(){return status;}public List<Candidate> getCandidates(){return candidates;}public String getReason(){return reason;}public int getExamined(){return examined;}
}
