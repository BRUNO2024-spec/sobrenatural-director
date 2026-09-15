package com.sobrenaturaldirector.situation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ThreatNarrativeAssessment {
    private final boolean eligible, optionalSuppressed, hardCooldown, recoveryActive;
    private final int scoreModifier, recentCount, sameRegionCount;
    private final List<String> reasons;

    public ThreatNarrativeAssessment(boolean eligible, boolean optionalSuppressed, boolean hardCooldown, boolean recoveryActive,
            int scoreModifier, int recentCount, int sameRegionCount, List<String> reasons) {
        this.eligible = eligible; this.optionalSuppressed = optionalSuppressed; this.hardCooldown = hardCooldown; this.recoveryActive = recoveryActive;
        this.scoreModifier = scoreModifier; this.recentCount = recentCount; this.sameRegionCount = sameRegionCount;
        this.reasons = Collections.unmodifiableList(new ArrayList<String>(reasons == null ? Collections.<String>emptyList() : reasons));
    }
    public boolean isEligible() { return eligible; }
    public boolean isOptionalSuppressed() { return optionalSuppressed; }
    public boolean isHardCooldown() { return hardCooldown; }
    public boolean isRecoveryActive() { return recoveryActive; }
    public int getScoreModifier() { return scoreModifier; }
    public int getRecentCount() { return recentCount; }
    public int getSameRegionCount() { return sameRegionCount; }
    public List<String> getReasons() { return reasons; }
}
