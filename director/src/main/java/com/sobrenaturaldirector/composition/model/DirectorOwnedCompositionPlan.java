package com.sobrenaturaldirector.composition.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.sobrenaturaldirector.domain.StableId;

/** Bounded, deterministic, Director-owned composition input. */
public final class DirectorOwnedCompositionPlan {
    public static final int SCHEMA_VERSION = 1;
    public static final int MAX_BLOCKS = 16;
    private final String compositionId, source, providerModId, providerVersion;
    private final int dimension, anchorX, anchorY, anchorZ;
    private final List<CompositionBlockOperation> operations;

    public DirectorOwnedCompositionPlan(String compositionId, String source, String providerModId,
            String providerVersion, int dimension, int anchorX, int anchorY, int anchorZ,
            List<CompositionBlockOperation> operations) {
        this.compositionId = StableId.require(compositionId, "compositionId");
        this.source = StableId.require(source, "source");
        this.providerModId = StableId.require(providerModId, "providerModId");
        this.providerVersion = StableId.require(providerVersion, "providerVersion");
        if (operations == null || operations.isEmpty() || operations.size() > MAX_BLOCKS) throw new IllegalArgumentException("invalid operation count");
        this.dimension = dimension; this.anchorX = anchorX; this.anchorY = anchorY; this.anchorZ = anchorZ;
        this.operations = Collections.unmodifiableList(new ArrayList<CompositionBlockOperation>(operations));
    }
    public String getCompositionId() { return compositionId; }
    public String getSource() { return source; }
    public String getProviderModId() { return providerModId; }
    public String getProviderVersion() { return providerVersion; }
    public int getDimension() { return dimension; }
    public int getAnchorX() { return anchorX; }
    public int getAnchorY() { return anchorY; }
    public int getAnchorZ() { return anchorZ; }
    public List<CompositionBlockOperation> getOperations() { return operations; }
}
