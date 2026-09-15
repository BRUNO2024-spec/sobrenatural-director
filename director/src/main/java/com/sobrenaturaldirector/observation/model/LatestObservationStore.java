package com.sobrenaturaldirector.observation.model;

public final class LatestObservationStore {
    private ObservationFrame latest;
    public void publish(ObservationFrame frame) { if (frame == null) throw new IllegalArgumentException("frame is required"); latest = frame; }
    public ObservationFrame getLatest() { return latest; }
    public void clear() { latest = null; }
}
