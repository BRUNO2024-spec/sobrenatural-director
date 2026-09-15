package com.sobrenaturaldirector.threat;

import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;
import com.sobrenaturaldirector.provider.DirectorContentProvider;
import net.minecraft.world.WorldServer;

public interface ThreatProvider extends DirectorContentProvider {
    ThreatDefinition getThreatDefinition();
    default String getExecutionVersion() { return getDetectedVersion(); }
    ThreatExecutionResult execute(ThreatExecutionRequest request, DirectorWorldSavedData saved);
    ThreatExecutionResult cleanup(ThreatExecutionRequest request, DirectorWorldSavedData saved);
    default ThreatPhysicalObservation inspectBinding(PersistentNarrativeThreat threat, ThreatPhysicalBinding binding, WorldServer world) {
        return ThreatPhysicalObservation.PROVIDER_UNAVAILABLE;
    }
}
