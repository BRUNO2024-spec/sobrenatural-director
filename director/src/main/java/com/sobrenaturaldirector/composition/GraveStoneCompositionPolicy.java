package com.sobrenaturaldirector.composition;

import com.sobrenaturaldirector.composition.model.ExternalBlockReference;
import cpw.mods.fml.common.Loader;

/** GraveStone-specific capability and allowlist boundary. */
public final class GraveStoneCompositionPolicy implements ExternalBlockPolicy {
    @Override public boolean isProviderAvailable() { return Loader.isModLoaded(GraveStoneCompositionAllowlist.MOD_ID); }
    @Override public boolean accepts(String providerModId, String providerVersion, ExternalBlockReference reference) {
        return GraveStoneCompositionAllowlist.MOD_ID.equals(providerModId)
                && GraveStoneCompositionAllowlist.VERSION.equals(providerVersion)
                && GraveStoneCompositionAllowlist.contains(reference);
    }
}
