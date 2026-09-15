package com.sobrenaturaldirector.execution;

/** Test-only stage boundary; production callers use the four-argument execute method. */
public interface ExecutionStageHook {
    boolean pauseAfter(ControlledProviderSlice slice);
}
