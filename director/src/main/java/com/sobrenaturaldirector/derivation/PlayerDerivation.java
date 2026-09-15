package com.sobrenaturaldirector.derivation;
import com.sobrenaturaldirector.model.player.PlayerModel;
public final class PlayerDerivation { private final PlayerModel model; private final DerivationTrace trace; public PlayerDerivation(PlayerModel model,DerivationTrace trace){this.model=model;this.trace=trace;} public PlayerModel getModel(){return model;} public DerivationTrace getTrace(){return trace;} }
