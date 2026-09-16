package com.sobrenaturaldirector.shadow;

import java.util.Arrays;

/** Immutable candidate DTO; deliberately contains no Minecraft/provider object. */
public final class ShadowCandidate {
    private final String id;
    private final String type;
    private final double[] numeric;
    private final int[] categorical;
    public ShadowCandidate(String id, String type, double[] numeric, int[] categorical) {
        if (id == null || id.length() == 0 || type == null || numeric == null || categorical == null) throw new IllegalArgumentException("invalid shadow candidate");
        this.id=id; this.type=type; this.numeric=Arrays.copyOf(numeric,numeric.length); this.categorical=Arrays.copyOf(categorical,categorical.length);
        for (double x:this.numeric) if (Double.isNaN(x)||Double.isInfinite(x)) throw new IllegalArgumentException("non-finite shadow feature");
        for (int x:this.categorical) if (x<0) throw new IllegalArgumentException("negative categorical id");
    }
    public String getId(){return id;} public String getType(){return type;}
    public double[] getNumeric(){return Arrays.copyOf(numeric,numeric.length);} public int[] getCategorical(){return Arrays.copyOf(categorical,categorical.length);}
}
