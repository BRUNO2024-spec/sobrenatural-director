package com.sobrenaturaldirector.action;

import com.sobrenaturaldirector.control.ControlRequest;
import com.sobrenaturaldirector.provider.DirectorProviderRegistry;

/** Provider-neutral binding from a semantic step to an existing control request. */
public interface ActionExecutionBinding {
    ActionType getActionType();
    boolean supports(ActionExecutionSpec spec);
    ControlRequest request(ActionExecutionSpec spec, DirectorProviderRegistry registry, long tick);
    ControlRequest compensation(ActionExecutionSpec spec, DirectorProviderRegistry registry, long tick);
}
