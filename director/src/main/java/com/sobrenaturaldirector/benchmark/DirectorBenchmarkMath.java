package com.sobrenaturaldirector.benchmark;

/** Versioned score math kept independent from production decision weights. */
public final class DirectorBenchmarkMath {
    private DirectorBenchmarkMath() { }
    public static double normalize(double value, double minimum, double maximum) {
        if (Double.isNaN(value) || Double.isInfinite(value) || maximum <= minimum) throw new IllegalArgumentException("invalid normalization");
        return Math.max(0, Math.min(100, (value - minimum) * 100.0 / (maximum - minimum)));
    }
    public static double composite(double hard, double quality, double temporal, double efficiency) {
        return hard * .40 + quality * .30 + temporal * .20 + efficiency * .10;
    }
    public static Double relativeImprovement(double candidate, double baseline) { return baseline == 0 ? null : candidate / baseline; }
    public static Double percentageImprovement(double candidate, double baseline) { return baseline == 0 ? null : ((candidate - baseline) / baseline) * 100.0; }
}
