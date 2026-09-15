package com.sobrenaturaldirector.narrative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NarrativePlanValidationResult {
    public enum Status { VALID, STALE, EXPIRED, TERMINAL, INVALID }
    private final Status status; private final List<String> reasons;
    public NarrativePlanValidationResult(Status status, List<String> reasons) { if (status == null) throw new IllegalArgumentException("status is required"); this.status=status; this.reasons=Collections.unmodifiableList(new ArrayList<String>(reasons == null ? Collections.<String>emptyList() : reasons)); }
    public Status getStatus(){return status;} public List<String> getReasons(){return reasons;} public boolean isValid(){return status==Status.VALID;}
}
