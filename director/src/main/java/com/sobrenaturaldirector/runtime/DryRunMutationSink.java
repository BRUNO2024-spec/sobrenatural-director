package com.sobrenaturaldirector.runtime;

import com.sobrenaturaldirector.mutation.model.MutationTransactionPlan;

/** Terminal boundary for 1J. It records plans and has no world access. */
public final class DryRunMutationSink {
    private int received;
    private int blockedAttempts;

    public void accept(MutationTransactionPlan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("mutation plan is required");
        }
        received++;
    }

    public void recordBlockedAttempt() {
        blockedAttempts++;
    }

    public int getReceived() {
        return received;
    }

    public int getBlockedAttempts() {
        return blockedAttempts;
    }
}
