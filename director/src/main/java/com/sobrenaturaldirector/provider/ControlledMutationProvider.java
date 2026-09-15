package com.sobrenaturaldirector.provider;

import com.sobrenaturaldirector.persistence.DirectorWorldSavedData;

/** Explicit provider mutation boundary; callers must supply an authorized structured request. */
public interface ControlledMutationProvider extends DirectorContentProvider {
    ProviderMutationResult createControlledNpc(ControlledNpcRequest request, DirectorWorldSavedData saved);
    ProviderMutationResult removeControlledNpc(ControlledNpcRequest request, DirectorWorldSavedData saved);
}
