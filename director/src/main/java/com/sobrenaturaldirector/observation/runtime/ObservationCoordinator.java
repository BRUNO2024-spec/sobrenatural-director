package com.sobrenaturaldirector.observation.runtime;

import com.sobrenaturaldirector.config.FoundationConfig;
import com.sobrenaturaldirector.observation.minecraft.MinecraftObservationCapture;
import com.sobrenaturaldirector.observation.model.LatestObservationStore;
import com.sobrenaturaldirector.observation.model.ObservationFrame;
import com.sobrenaturaldirector.observation.model.ObservationStatus;
import net.minecraft.server.MinecraftServer;

public final class ObservationCoordinator {
    private final FoundationConfig configuration;
    private final MinecraftObservationCapture capture = new MinecraftObservationCapture();
    private final LatestObservationStore store = new LatestObservationStore();
    private final ObservationMetrics metrics = new ObservationMetrics();
    private long lastCaptureTick = Long.MIN_VALUE;
    private long sequence;

    public ObservationCoordinator(FoundationConfig configuration) { if (configuration == null) throw new IllegalArgumentException("configuration is required"); this.configuration=configuration; }
    public void onServerTick(MinecraftServer server, long tick) {
        if (!configuration.isEnabled() || !configuration.isObservationEnabled()) return;
        if (lastCaptureTick != Long.MIN_VALUE && tick >= lastCaptureTick && tick - lastCaptureTick < configuration.getObservationIntervalTicks()) return;
        if (lastCaptureTick != Long.MIN_VALUE && tick < lastCaptureTick) lastCaptureTick = Long.MIN_VALUE;
        lastCaptureTick=tick; long started=System.nanoTime(); metrics.attempted();
        ObservationFrame frame=capture.capture(server,tick,sequence++); store.publish(frame); long elapsed=System.nanoTime()-started;
        if(frame.getDiagnostics().getStatus()==ObservationStatus.COMPLETE)metrics.completed(elapsed,frame.getWorlds().size(),frame.getPlayers().size());else if(frame.getDiagnostics().getStatus()==ObservationStatus.PARTIAL)metrics.partial(elapsed,frame.getWorlds().size(),frame.getPlayers().size());else metrics.failed(elapsed);
    }
    public LatestObservationStore getStore(){return store;} public ObservationMetrics getMetrics(){return metrics;} public long getLastCaptureTick(){return lastCaptureTick;}
}
