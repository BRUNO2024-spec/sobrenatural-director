package com.sobrenaturaldirector.composition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.sobrenaturaldirector.composition.model.ExternalBlockReference;

/** Registry references selected from the 1L.2 runtime snapshot. */
public final class GraveStoneCompositionAllowlist {
    public static final String MOD_ID = "GraveStone";
    public static final String VERSION = "2.13.0";
    private static final Set<String> NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("GSBoneBlock", "GSBoneSlab", "GSBoneStairs")));
    private GraveStoneCompositionAllowlist() { }
    public static boolean contains(ExternalBlockReference reference) {
        return reference != null && MOD_ID.equals(reference.getProviderModId()) && NAMES.contains(reference.getRegistryName()) && reference.getMetadata() == 0;
    }
    public static Set<String> names() { return NAMES; }
}
