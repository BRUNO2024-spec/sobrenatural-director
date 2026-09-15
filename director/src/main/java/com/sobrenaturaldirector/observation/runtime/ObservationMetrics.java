package com.sobrenaturaldirector.observation.runtime;

public final class ObservationMetrics {
    private long capturesAttempted, capturesSucceeded, capturesPartial, capturesFailed;
    private long lastCaptureDurationNanos;
    private int lastPlayerCount, lastWorldCount;
    public void attempted() { capturesAttempted++; }
    public void completed(long nanos, int worlds, int players) { capturesSucceeded++; lastCaptureDurationNanos=nanos;lastWorldCount=worlds;lastPlayerCount=players; }
    public void partial(long nanos, int worlds, int players) { capturesPartial++; lastCaptureDurationNanos=nanos;lastWorldCount=worlds;lastPlayerCount=players; }
    public void failed(long nanos) { capturesFailed++; lastCaptureDurationNanos=nanos; }
    public long getCapturesAttempted(){return capturesAttempted;} public long getCapturesSucceeded(){return capturesSucceeded;} public long getCapturesPartial(){return capturesPartial;} public long getCapturesFailed(){return capturesFailed;} public long getLastCaptureDurationNanos(){return lastCaptureDurationNanos;} public int getLastPlayerCount(){return lastPlayerCount;} public int getLastWorldCount(){return lastWorldCount;}
}
