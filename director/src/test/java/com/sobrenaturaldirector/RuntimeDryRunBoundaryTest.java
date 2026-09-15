package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;

import com.sobrenaturaldirector.runtime.DirectorRuntimeMode;
import com.sobrenaturaldirector.runtime.DryRunMutationSink;
import com.sobrenaturaldirector.mutation.model.MutationBudget;
import com.sobrenaturaldirector.mutation.model.MutationTransactionPlan;
import com.sobrenaturaldirector.mutation.safety.MutationSafetyAssessment;
import com.sobrenaturaldirector.mutation.model.ExecutionReadiness;

public final class RuntimeDryRunBoundaryTest {
    @Test
    public void onlyDryRunModeExists() {
        assertEquals(2, DirectorRuntimeMode.values().length);
        assertEquals(DirectorRuntimeMode.DRY_RUN, DirectorRuntimeMode.values()[0]);
        assertEquals(DirectorRuntimeMode.CONTROLLED_EXECUTION, DirectorRuntimeMode.values()[1]);
    }

    @Test
    public void sinkCapturesPlanWithoutExecutionSurface() {
        DryRunMutationSink sink = new DryRunMutationSink();
        MutationTransactionPlan plan = new MutationTransactionPlan("dry-run:test", Collections.emptyList(),
                Collections.<String>emptyList(), new MutationBudget(1, 1, 1, 0, 1, 0, 1),
                new MutationSafetyAssessment(MutationSafetyAssessment.Status.BLOCKED,
                        ExecutionReadiness.SITE_REQUIRED, Collections.<String>emptyList(),
                        Collections.<String>emptyList()), 0);
        sink.accept(plan);
        assertEquals(1, sink.getReceived());
        assertEquals(0, sink.getBlockedAttempts());
        assertNotNull(plan.getSafety());
    }
}
