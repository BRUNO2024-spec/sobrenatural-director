package com.sobrenaturaldirector.capability;

import java.util.List;

public interface CapabilityConstraint {
    boolean accepts(SituationIntent intent, DirectorContext context, List<CapabilityDescriptor> selected);
}
