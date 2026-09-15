package com.sobrenaturaldirector.observation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ObservationDiagnostics {
    public static final int MAX_WARNINGS = 32;
    private final ObservationStatus status;
    private final int sectionsAttempted;
    private final int sectionsSucceeded;
    private final List<String> warnings;

    public ObservationDiagnostics(ObservationStatus status, int attempted, int succeeded, List<String> warnings) {
        if (status == null || attempted < 0 || succeeded < 0 || succeeded > attempted || warnings == null || warnings.size() > MAX_WARNINGS) {
            throw new IllegalArgumentException("invalid observation diagnostics");
        }
        this.status = status;
        sectionsAttempted = attempted;
        sectionsSucceeded = succeeded;
        List<String> copy = new ArrayList<String>(warnings);
        for (String warning : copy) if (warning == null || warning.length() > 128) throw new IllegalArgumentException("invalid observation warning");
        this.warnings = Collections.unmodifiableList(copy);
    }

    public ObservationStatus getStatus() { return status; }
    public int getSectionsAttempted() { return sectionsAttempted; }
    public int getSectionsSucceeded() { return sectionsSucceeded; }
    public List<String> getWarnings() { return warnings; }
    @Override public boolean equals(Object other) { if (!(other instanceof ObservationDiagnostics)) return false; ObservationDiagnostics x=(ObservationDiagnostics)other; return status==x.status && sectionsAttempted==x.sectionsAttempted && sectionsSucceeded==x.sectionsSucceeded && warnings.equals(x.warnings); }
    @Override public int hashCode() { return java.util.Arrays.hashCode(new Object[]{status, sectionsAttempted, sectionsSucceeded, warnings}); }
}
