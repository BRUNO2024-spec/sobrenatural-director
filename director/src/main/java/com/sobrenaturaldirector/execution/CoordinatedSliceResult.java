package com.sobrenaturaldirector.execution;

public final class CoordinatedSliceResult {
    public enum Status { EXECUTED, RECONCILED, FAILED, COMPENSATED, STALE, REJECTED }
    private final Status status;
    private final String detail;
    public CoordinatedSliceResult(Status status, String detail) {
        if (status == null) throw new IllegalArgumentException("status is required");
        this.status = status; this.detail = detail == null ? "" : detail;
    }
    public Status getStatus() { return status; }
    public String getDetail() { return detail; }
    public boolean succeeded() { return status == Status.EXECUTED || status == Status.RECONCILED || status == Status.COMPENSATED; }
    public static CoordinatedSliceResult of(Status status, String detail) { return new CoordinatedSliceResult(status, detail); }
}
