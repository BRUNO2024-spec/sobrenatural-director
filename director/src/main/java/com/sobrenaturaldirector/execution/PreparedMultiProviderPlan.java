package com.sobrenaturaldirector.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import com.sobrenaturaldirector.capability.CandidatePlan;
import com.sobrenaturaldirector.domain.StableId;

public final class PreparedMultiProviderPlan {
    private final CandidatePlan plan;
    private final String fingerprint;
    private final List<ControlledProviderSlice> slices;
    public PreparedMultiProviderPlan(CandidatePlan plan, String fingerprint, List<ControlledProviderSlice> slices) {
        if (plan == null || fingerprint == null || slices == null || slices.isEmpty()) throw new IllegalArgumentException("prepared plan is incomplete");
        this.plan = plan; this.fingerprint = StableId.require(fingerprint, "fingerprint");
        List<ControlledProviderSlice> ordered = new ArrayList<ControlledProviderSlice>(slices);
        for (ControlledProviderSlice slice : ordered) if (slice == null) throw new IllegalArgumentException("invalid slice");
        Collections.sort(ordered, new Comparator<ControlledProviderSlice>() { public int compare(ControlledProviderSlice a, ControlledProviderSlice b) { int c = Integer.compare(a.getPhase().getOrder(), b.getPhase().getOrder()); return c != 0 ? c : a.getSliceId().compareTo(b.getSliceId()); } });
        Set<String> ids = new HashSet<String>();
        for (ControlledProviderSlice slice : ordered) if (slice.getSliceId() == null || slice.getPhase() == null || slice.getProviderId() == null || !ids.add(slice.getSliceId())) throw new IllegalArgumentException("invalid or duplicate slice");
        this.slices = Collections.unmodifiableList(ordered);
    }
    public CandidatePlan getPlan() { return plan; }
    public String getFingerprint() { return fingerprint; }
    public List<ControlledProviderSlice> getSlices() { return slices; }
}
