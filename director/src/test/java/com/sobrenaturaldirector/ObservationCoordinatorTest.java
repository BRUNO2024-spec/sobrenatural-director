package com.sobrenaturaldirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import com.sobrenaturaldirector.config.FoundationConfig;
import com.sobrenaturaldirector.observation.model.ObservationStatus;
import com.sobrenaturaldirector.observation.runtime.ObservationCoordinator;
import org.junit.Test;

public class ObservationCoordinatorTest {
    @Test public void intervalGateCapturesAtStartAndExpiry() {
        ObservationCoordinator coordinator = new ObservationCoordinator(FoundationConfig.defaults());
        coordinator.onServerTick(null, 0); assertNotNull(coordinator.getStore().getLatest()); assertEquals(1, coordinator.getMetrics().getCapturesAttempted());
        coordinator.onServerTick(null, 1); assertEquals(1, coordinator.getMetrics().getCapturesAttempted());
        coordinator.onServerTick(null, 199); assertEquals(1, coordinator.getMetrics().getCapturesAttempted());
        coordinator.onServerTick(null, 200); assertEquals(2, coordinator.getMetrics().getCapturesAttempted()); assertEquals(ObservationStatus.FAILED, coordinator.getStore().getLatest().getDiagnostics().getStatus());
    }

    @Test public void tickRollbackResetsTransientGate() {
        ObservationCoordinator coordinator = new ObservationCoordinator(FoundationConfig.defaults()); coordinator.onServerTick(null, 200); coordinator.onServerTick(null, 10); assertEquals(2, coordinator.getMetrics().getCapturesAttempted());
    }

}
