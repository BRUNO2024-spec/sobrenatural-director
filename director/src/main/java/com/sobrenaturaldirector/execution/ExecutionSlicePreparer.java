package com.sobrenaturaldirector.execution;

import com.sobrenaturaldirector.capability.CapabilityDescriptor;
import com.sobrenaturaldirector.capability.CandidatePlan;

/** Generic preparation boundary; provider implementation details stay outside the factory. */
public interface ExecutionSlicePreparer {
    boolean supports(CapabilityDescriptor capability);
    ControlledProviderSlice prepare(CandidatePlan plan, CapabilityDescriptor capability, ExecutionPreparationContext context);
}
